/* ArboNotes – frontend controller.
 *
 * The Rust side owns all filesystem access (notes, tree.json, search,
 * calendar date discovery). This file mirrors the JavaFX UI: a draggable
 * tree, a calendar of daily notes, and a Quill editor that saves notes as
 * HTML fragments.
 */
"use strict";

const tauriInvoke = window.__TAURI__ && window.__TAURI__.core
    ? window.__TAURI__.core.invoke
    : null;

async function invoke(command, args) {
    if (!tauriInvoke) {
        throw new Error("Tauri IPC is not available. Run the app through Tauri.");
    }
    return tauriInvoke(command, args);
}

const state = {
    config: null,
    tree: null,
    selectedId: null,
    expanded: new Set(),
    editorSource: "NONE",
    currentTreeId: null,
    currentDailyDate: null,
    draggedId: null,
    openToken: 0,
    lastSavedHtml: "",
    calendar: {
        year: 0,
        month: 0,
        selected: null,
        dates: new Set(),
    },
};

let quill = null;
let nextNodeId = 0;
let autosaveTimer = null;
let operationChain = Promise.resolve();
let calendarToken = 0;

/* =====================================================================
 * Small helpers
 * ===================================================================== */

function errorText(error) {
    if (typeof error === "string") return error;
    if (error && typeof error.message === "string") return error.message;
    return String(error);
}

function setStatus(text) {
    document.getElementById("status-label").textContent = text;
}

function clamp(value, min, max) {
    return Math.min(max, Math.max(min, value));
}

function pad2(value) {
    return String(value).padStart(2, "0");
}

function dateKey(date) {
    return `${date.getFullYear()}${pad2(date.getMonth() + 1)}${pad2(date.getDate())}`;
}

function isoDate(date) {
    return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
}

function parseDateKey(key) {
    return new Date(
        Number(key.slice(0, 4)),
        Number(key.slice(4, 6)) - 1,
        Number(key.slice(6, 8)),
    );
}

function sameDay(a, b) {
    return a && b
        && a.getFullYear() === b.getFullYear()
        && a.getMonth() === b.getMonth()
        && a.getDate() === b.getDate();
}

function stripHtml(value) {
    return value.replace(/<[^>]*>/g, "").trim();
}

/* =====================================================================
 * Serialized backend operations
 * ===================================================================== */

function enqueue(operation) {
    const result = operationChain.then(operation, operation);
    operationChain = result.catch(() => {});
    return result;
}

function queueSave() {
    return enqueue(() => saveCurrentEditorContent())
        .catch((error) => showError("Could not save note", errorText(error)));
}

/* =====================================================================
 * Quill editor
 * ===================================================================== */

function stripQuillUi(html) {
    if (!html || html.indexOf("ql-ui") === -1) return html;
    const holder = document.createElement("div");
    holder.innerHTML = html;
    const junk = holder.querySelectorAll(".ql-ui");
    for (let i = junk.length - 1; i >= 0; i--) {
        junk[i].parentNode.removeChild(junk[i]);
    }
    return holder.innerHTML;
}

function getEditorHtml() {
    if (!quill) return "";
    return stripQuillUi(quill.root.innerHTML);
}

function setEditorHtml(html) {
    if (!quill) return;
    try {
        if (!html || html.length === 0) {
            quill.setContents([], "silent");
        } else {
            const delta = quill.clipboard.convert({ html: stripQuillUi(html) });
            quill.setContents(delta, "silent");
        }
    } catch (error) {
        // If Quill cannot parse a malformed note, fall back to plain text so
        // the user can still recover the content.
        quill.setText(String(html || ""), "silent");
    }
    state.lastSavedHtml = getEditorHtml();
}

function initQuill() {
    quill = new Quill("#editor-container", {
        theme: "snow",
        modules: {
            table: true,
            syntax: true,
            toolbar: {
                container: "#toolbar",
                handlers: {
                    table: function (value) {
                        if (value === "insert-table") {
                            const table = this.quill.getModule("table");
                            if (table) table.insertTable(2, 2);
                        }
                    },
                    "table-row-above": function () {
                        const table = this.quill.getModule("table");
                        if (table) table.insertRowAbove();
                    },
                    "table-row-below": function () {
                        const table = this.quill.getModule("table");
                        if (table) table.insertRowBelow();
                    },
                    "table-col-left": function () {
                        const table = this.quill.getModule("table");
                        if (table) table.insertColumnLeft();
                    },
                    "table-col-right": function () {
                        const table = this.quill.getModule("table");
                        if (table) table.insertColumnRight();
                    },
                    "table-delete-row": function () {
                        const table = this.quill.getModule("table");
                        if (table) table.deleteRow();
                    },
                    "table-delete-col": function () {
                        const table = this.quill.getModule("table");
                        if (table) table.deleteColumn();
                    },
                    "table-delete": function () {
                        const table = this.quill.getModule("table");
                        if (table) table.deleteTable();
                    },
                },
            },
        },
    });

    quill.on("text-change", (delta, oldDelta, source) => {
        if (source !== "user") return;
        if (autosaveTimer) clearTimeout(autosaveTimer);
        autosaveTimer = setTimeout(() => {
            autosaveTimer = null;
            queueSave();
        }, 600);
    });

    // Expose the same helpers the old standalone editor.html provided.
    window.getContent = getEditorHtml;
    window.setContent = setEditorHtml;
}

/* =====================================================================
 * Saving and opening notes
 * ===================================================================== */

async function saveCurrentEditorContent() {
    if (autosaveTimer) {
        clearTimeout(autosaveTimer);
        autosaveTimer = null;
    }
    if (!quill || state.editorSource === "NONE") return;

    const html = getEditorHtml();
    // Match the Java application: an editor that only contains empty markup
    // is treated as "nothing to save".
    if (stripHtml(html) === "") return;
    if (html === state.lastSavedHtml) return;

    let fileName = null;
    if (state.editorSource === "TREE_ITEM" && state.currentTreeId) {
        const node = nodeById(state.currentTreeId);
        if (node) fileName = node.fileName;
    } else if (state.editorSource === "DAILY_NOTE" && state.currentDailyDate) {
        fileName = `${state.currentDailyDate}.html`;
    }
    if (!fileName) return;

    await invoke("save_note", { fileName, html });
    state.lastSavedHtml = html;
}

function openTreeNote(node) {
    const token = ++state.openToken;

    return enqueue(async () => {
        if (token !== state.openToken) return;

        try {
            await saveCurrentEditorContent();
        } catch (error) {
            showError("Could not save note", errorText(error));
        }
        if (token !== state.openToken) return;

        setStatus(` Selected: ${node.nodeName}   >>   FileName: ${node.fileName}`);

        try {
            const html = await invoke("load_note", { fileName: node.fileName });
            if (token !== state.openToken) return;
            state.editorSource = "TREE_ITEM";
            state.currentTreeId = node._id;
            state.currentDailyDate = null;
            setEditorHtml(html);
        } catch (error) {
            showError("Could not open note", errorText(error));
        }
    });
}

function openDailyNote(key) {
    const token = ++state.openToken;

    return enqueue(async () => {
        if (token !== state.openToken) return;

        try {
            await saveCurrentEditorContent();
        } catch (error) {
            showError("Could not save note", errorText(error));
        }
        if (token !== state.openToken) return;

        const fileName = `${key}.html`;
        setStatus(` Daily note: ${fileName}`);

        try {
            // Opening a day must not create an empty file. The note only comes
            // into existence once the user types something and it is saved.
            const exists = await invoke("note_exists", { fileName });
            if (token !== state.openToken) return;
            const html = exists ? await invoke("load_note", { fileName }) : "";
            if (token !== state.openToken) return;
            state.editorSource = "DAILY_NOTE";
            state.currentDailyDate = key;
            state.currentTreeId = null;
            setEditorHtml(html);
        } catch (error) {
            showError("Could not open daily note", errorText(error));
        }
    });
}

/* =====================================================================
 * Tree model helpers
 * ===================================================================== */

function assignIds(node) {
    node._id = `n${nextNodeId++}`;
    if (!Array.isArray(node.children)) node.children = [];
    node.children.forEach(assignIds);
}

function stripInternal(node) {
    return {
        nodeName: node.nodeName,
        fileName: node.fileName,
        children: (node.children || []).map(stripInternal),
    };
}

function findPath(node, id, path = []) {
    if (!node) return null;
    const nextPath = path.concat(node);
    if (node._id === id) return nextPath;
    for (const child of node.children || []) {
        const found = findPath(child, id, nextPath);
        if (found) return found;
    }
    return null;
}

function nodeById(id) {
    if (!id) return null;
    const path = findPath(state.tree, id);
    return path ? path[path.length - 1] : null;
}

function parentOf(node) {
    if (!node) return null;
    const path = findPath(state.tree, node._id);
    return path && path.length > 1 ? path[path.length - 2] : null;
}

function isDescendant(ancestorId, nodeId) {
    const path = findPath(state.tree, nodeId);
    return !!path && path.some((node) => node._id === ancestorId);
}

async function saveTree() {
    await invoke("save_tree", { tree: stripInternal(state.tree) });
}

async function selectNode(node, options = {}) {
    const { open = true, scroll = false } = options;
    state.selectedId = node._id;
    renderTree();
    if (open) await openTreeNote(node);
    if (scroll) scrollToNode(node._id);
}

function scrollToNode(id) {
    requestAnimationFrame(() => {
        const row = document.querySelector(`.tree-row[data-id="${id}"]`);
        if (row) row.scrollIntoView({ block: "nearest" });
    });
}

/* =====================================================================
 * Tree rendering and interaction
 * ===================================================================== */

function clearDragClasses() {
    document.querySelectorAll(".tree-row.drag-over, .tree-row.drag-over-edge")
        .forEach((row) => row.classList.remove("drag-over", "drag-over-edge"));
}

function renderTree() {
    const treeRoot = document.getElementById("tree");
    const scroll = document.getElementById("tree-scroll");
    const previousScroll = scroll.scrollTop;

    treeRoot.replaceChildren();
    if (state.tree) treeRoot.append(renderTreeNode(state.tree, true));

    scroll.scrollTop = previousScroll;
}

function renderTreeNode(node, isRoot) {
    const li = document.createElement("li");
    const row = document.createElement("div");
    row.className = "tree-row";
    if (state.selectedId === node._id) row.classList.add("selected");
    row.dataset.id = node._id;
    row.draggable = !isRoot;
    row.setAttribute("role", "treeitem");

    const hasChildren = (node.children || []).length > 0;
    const expanded = state.expanded.has(node._id);
    row.setAttribute("aria-expanded", String(hasChildren ? expanded : false));

    const twisty = document.createElement("span");
    twisty.className = "tree-twisty" + (hasChildren ? "" : " empty");
    twisty.textContent = hasChildren ? (expanded ? "\u25be" : "\u25b8") : "";
    twisty.addEventListener("click", (event) => {
        event.stopPropagation();
        if (!hasChildren) return;
        if (state.expanded.has(node._id)) {
            state.expanded.delete(node._id);
        } else {
            state.expanded.add(node._id);
        }
        renderTree();
    });

    const label = document.createElement("span");
    label.className = "tree-label";
    label.textContent = node.nodeName;

    row.append(twisty, label);
    li.append(row);

    row.addEventListener("click", () => {
        if (state.selectedId === node._id) return;
        void selectNode(node);
    });

    row.addEventListener("dblclick", (event) => {
        event.preventDefault();
        state.selectedId = node._id;
        renderTree();
        void openTreeNote(node);
    });

    row.addEventListener("contextmenu", (event) => {
        event.preventDefault();
        event.stopPropagation();
        state.selectedId = node._id;
        renderTree();
        showTreeContextMenu(node, event.clientX, event.clientY);
    });

    if (!isRoot) {
        row.addEventListener("dragstart", (event) => {
            state.draggedId = node._id;
            event.dataTransfer.effectAllowed = "move";
            event.dataTransfer.setData("text/plain", node._id);
        });
    }

    row.addEventListener("dragover", (event) => {
        const dragged = nodeById(state.draggedId);
        if (!dragged || dragged._id === node._id) return;
        if (isDescendant(dragged._id, node._id)) return;

        const rect = row.getBoundingClientRect();
        const offset = event.clientY - rect.top;
        const onEdge = offset < rect.height * 0.25 || offset > rect.height * 0.75;
        if (onEdge && isRoot) return;

        event.preventDefault();
        event.dataTransfer.dropEffect = "move";
        clearDragClasses();
        row.classList.add(onEdge ? "drag-over-edge" : "drag-over");
    });

    row.addEventListener("dragleave", () => {
        row.classList.remove("drag-over", "drag-over-edge");
    });

    row.addEventListener("drop", (event) => {
        event.preventDefault();
        event.stopPropagation();

        const dragged = nodeById(state.draggedId);
        state.draggedId = null;
        clearDragClasses();
        if (!dragged || dragged._id === node._id) return;
        if (isDescendant(dragged._id, node._id)) return;

        const rect = row.getBoundingClientRect();
        const offset = event.clientY - rect.top;
        const onEdge = offset < rect.height * 0.25 || offset > rect.height * 0.75;
        const onBottomEdge = offset > rect.height * 0.75;
        if (onEdge && isRoot) return;

        const oldParent = parentOf(dragged);
        if (!oldParent) return;
        const oldIndex = oldParent.children.indexOf(dragged);
        if (oldIndex >= 0) oldParent.children.splice(oldIndex, 1);

        if (onEdge) {
            const newParent = parentOf(node) || state.tree;
            let index = newParent.children.indexOf(node);
            if (index < 0) index = newParent.children.length;
            if (onBottomEdge) index += 1;
            newParent.children.splice(index, 0, dragged);
            state.expanded.add(newParent._id);
        } else {
            node.children.push(dragged);
            state.expanded.add(node._id);
        }

        state.selectedId = dragged._id;
        renderTree();
        void saveTree().catch((error) => showError("Could not save the note tree", errorText(error)));

        if (state.currentTreeId !== dragged._id) {
            void openTreeNote(dragged);
        }
    });

    row.addEventListener("dragend", () => {
        state.draggedId = null;
        clearDragClasses();
    });

    if (hasChildren && expanded) {
        const childList = document.createElement("ul");
        childList.setAttribute("role", "group");
        for (const child of node.children) {
            childList.append(renderTreeNode(child, false));
        }
        li.append(childList);
    }

    return li;
}

/* =====================================================================
 * Tree context menu actions
 * ===================================================================== */

function showTreeContextMenu(node, x, y) {
    showContextMenu(x, y, [
        {
            label: "Create...",
            action: () => createChildNode(node),
        },
        {
            label: "Rename",
            action: () => renameNode(node),
        },
        {
            label: "Delete",
            disabled: node === state.tree,
            action: () => deleteNode(node),
        },
        {
            label: "Search",
            action: () => openSearchDialog(node),
        },
    ]);
}

async function createChildNode(parent) {
    const rawName = await showPrompt("Enter a name...", "");
    if (rawName === null || rawName === "") return;

    try {
        const fileName = await invoke("create_note");
        const nodeName = await invoke("sanitize_name", { raw: rawName });
        const child = { nodeName, fileName, children: [] };
        assignIds(child);

        parent.children.push(child);
        state.expanded.add(parent._id);
        await saveTree();

        await selectNode(child);
        const heading = await invoke("heading_for_name", { raw: rawName });
        setEditorHtml(heading);
        // The heading is new content that is not on disk yet.
        state.lastSavedHtml = "";
        await queueSave();
    } catch (error) {
        showError("Could not create note", errorText(error));
    }
}

async function renameNode(node) {
    const rawName = await showPrompt("Enter a new name...", node.nodeName);
    if (rawName === null || rawName === "") return;

    try {
        node.nodeName = await invoke("sanitize_name", { raw: rawName });
        await saveTree();
        renderTree();
        if (state.selectedId === node._id) {
            setStatus(` Selected: ${node.nodeName}   >>   FileName: ${node.fileName}`);
        }
    } catch (error) {
        showError("Could not rename note", errorText(error));
    }
}

async function deleteNode(node) {
    if (node === state.tree) return;

    const answer = await showConfirm("Are you sure?");
    if (answer !== "yes") return;

    const parent = parentOf(node) || state.tree;
    const index = parent.children.indexOf(node);
    if (index >= 0) parent.children.splice(index, 1);

    if (state.currentTreeId === node._id) {
        state.currentTreeId = null;
        state.currentDailyDate = null;
        state.editorSource = "NONE";
        setEditorHtml("");
    }

    try {
        await invoke("delete_note", { fileName: node.fileName });
    } catch (error) {
        showError("Could not delete note file", errorText(error));
    }

    try {
        await saveTree();
    } catch (error) {
        showError("Could not save the note tree", errorText(error));
    }

    await selectNode(parent);
}

/* =====================================================================
 * Search dialog
 * ===================================================================== */

function collectSearchTargets(node, targets, seen, nodesByFileName) {
    if (!node) return;
    if (!seen.has(node.fileName)) {
        seen.add(node.fileName);
        nodesByFileName.set(node.fileName, node);
        targets.push({ nodeName: node.nodeName, fileName: node.fileName });
    }
    for (const child of node.children || []) {
        collectSearchTargets(child, targets, seen, nodesByFileName);
    }
}

function openSearchDialog(startNode) {
    const targets = [];
    const nodesByFileName = new Map();
    collectSearchTargets(startNode, targets, new Set(), nodesByFileName);

    const fragment = document.createDocumentFragment();

    const searchLabel = document.createElement("div");
    searchLabel.textContent = "Search for text in files:";

    const searchBox = document.createElement("div");
    searchBox.className = "search-box";

    const searchInput = document.createElement("input");
    searchInput.type = "text";
    searchInput.placeholder = "Search text...";

    const searchButton = document.createElement("button");
    searchButton.type = "button";
    searchButton.className = "modal-button";
    searchButton.textContent = "Search";

    searchBox.append(searchInput, searchButton);

    const resultsLabel = document.createElement("div");
    resultsLabel.textContent = "Files containing search text:";

    const results = document.createElement("div");
    results.className = "search-results";

    fragment.append(searchLabel, searchBox, resultsLabel, results);

    const performSearch = async () => {
        const searchText = searchInput.value.trim();
        if (searchText === "") return;

        searchButton.disabled = true;
        searchButton.textContent = "Searching...";
        results.replaceChildren();

        try {
            const found = await invoke("search_notes", {
                targets,
                searchText,
            });

            if (found.length === 0) {
                const empty = document.createElement("div");
                empty.className = "search-empty";
                empty.textContent = "No files contain that text.";
                results.append(empty);
                return;
            }

            for (const item of found) {
                const targetNode = nodesByFileName.get(item.fileName);
                if (!targetNode) continue;

                const row = document.createElement("div");
                row.className = "search-result";
                row.textContent = item.nodeName;
                row.title = item.fileName;

                row.addEventListener("click", () => {
                    results.querySelectorAll(".search-result.selected")
                        .forEach((selected) => selected.classList.remove("selected"));
                    row.classList.add("selected");
                });

                row.addEventListener("dblclick", () => {
                    const path = findPath(state.tree, targetNode._id) || [];
                    path.forEach((node) => state.expanded.add(node._id));
                    renderTree();
                    closeModal("close");
                    void selectNode(targetNode, { scroll: true });
                });

                results.append(row);
            }
        } catch (error) {
            const message = document.createElement("div");
            message.className = "search-empty";
            message.textContent = `Search failed: ${errorText(error)}`;
            results.append(message);
        } finally {
            searchButton.disabled = false;
            searchButton.textContent = "Search";
        }
    };

    searchButton.addEventListener("click", () => void performSearch());
    searchInput.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            void performSearch();
        }
    });

    openModal({
        title: "Search Files",
        body: fragment,
        buttons: [{ label: "Close", value: "close" }],
        cancelValue: "close",
        focus: searchInput,
    });
}

/* =====================================================================
 * Calendar
 * ===================================================================== */

function firstDayOfWeek() {
    try {
        const locale = new Intl.Locale(navigator.language || "en-US");
        const info = locale.weekInfo
            || (typeof locale.getWeekInfo === "function" ? locale.getWeekInfo() : null);
        if (info && info.firstDay) return info.firstDay % 7;
    } catch (error) {
        // Fall through to a conservative default.
    }
    return (navigator.language || "").toLowerCase().startsWith("en-us") ? 0 : 1;
}

function buildWeekdayRow() {
    const row = document.getElementById("calendar-weekdays");
    row.replaceChildren();
    const first = firstDayOfWeek();
    // 2021-08-01 was a Sunday; use it as a stable base for weekday names.
    for (let i = 0; i < 7; i++) {
        const dayIndex = (first + i) % 7;
        const date = new Date(2021, 7, 1 + dayIndex);
        const label = document.createElement("div");
        label.className = "calendar-weekday";
        label.textContent = date.toLocaleDateString(undefined, { weekday: "short" });
        row.append(label);
    }
}

function initCalendar() {
    const today = new Date();
    state.calendar.selected = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    state.calendar.year = today.getFullYear();
    state.calendar.month = today.getMonth();
    buildWeekdayRow();
    void refreshCalendar();
}

async function refreshCalendar() {
    const token = ++calendarToken;
    const { year, month } = state.calendar;

    let dates = new Set();
    try {
        const found = await invoke("dates_with_notes", { year, month: month + 1 });
        dates = new Set(found);
    } catch (error) {
        showError("Could not read daily notes", errorText(error));
    }

    if (token !== calendarToken) return;
    state.calendar.dates = dates;
    renderCalendar();
}

function renderCalendar() {
    const { year, month, selected, dates } = state.calendar;

    const monthName = new Date(year, month, 1)
        .toLocaleDateString(undefined, { month: "long" });
    document.getElementById("calendar-title").textContent = `${monthName} ${year}`;

    const grid = document.getElementById("calendar-grid");
    grid.replaceChildren();

    const first = new Date(year, month, 1);
    const leadingBlanks = (first.getDay() - firstDayOfWeek() + 7) % 7;
    const start = new Date(year, month, 1 - leadingBlanks);
    const today = new Date();
    const todayDate = new Date(today.getFullYear(), today.getMonth(), today.getDate());

    for (let i = 0; i < 42; i++) {
        const date = new Date(start.getFullYear(), start.getMonth(), start.getDate() + i);
        const key = dateKey(date);

        const cell = document.createElement("div");
        cell.className = "calendar-day";
        cell.textContent = String(date.getDate());
        cell.dataset.date = key;
        cell.title = isoDate(date);
        cell.tabIndex = 0;
        cell.setAttribute("role", "button");

        if (date.getMonth() !== month) cell.classList.add("calendar-day-other");
        if (sameDay(date, todayDate)) cell.classList.add("calendar-day-today");
        if (sameDay(date, selected)) cell.classList.add("calendar-day-selected");
        if (dates.has(key)) cell.classList.add("calendar-day-with-note");

        const openThisDay = () => {
            state.calendar.selected = date;
            renderCalendar();
            void openDailyNote(key).then(() => refreshCalendar());
        };

        cell.addEventListener("click", openThisDay);
        cell.addEventListener("keydown", (event) => {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                openThisDay();
            }
        });

        cell.addEventListener("contextmenu", (event) => {
            event.preventDefault();
            event.stopPropagation();
            void showCalendarContextMenu(key, event.clientX, event.clientY);
        });

        grid.append(cell);
    }
}

function shiftCalendar(months) {
    const shifted = new Date(state.calendar.year, state.calendar.month + months, 1);
    state.calendar.year = shifted.getFullYear();
    state.calendar.month = shifted.getMonth();
    void refreshCalendar();
}

async function showCalendarContextMenu(key, x, y) {
    const fileName = `${key}.html`;
    let hasNote = false;
    try {
        hasNote = await invoke("note_exists", { fileName });
    } catch (error) {
        hasNote = false;
    }

    const date = parseDateKey(key);
    const items = [
        {
            label: "Open note",
            action: () => {
                state.calendar.selected = date;
                renderCalendar();
                void openDailyNote(key).then(() => refreshCalendar());
            },
        },
        {
            label: "Delete note",
            disabled: !hasNote,
            action: async () => {
                const answer = await showConfirm(`Delete note for ${isoDate(date)}?`);
                if (answer !== "yes") return;

                try {
                    await invoke("delete_note", { fileName });
                    if (state.currentDailyDate === key) {
                        state.currentDailyDate = null;
                        state.editorSource = "NONE";
                        setEditorHtml("");
                        setStatus("No file open");
                    }
                    await refreshCalendar();
                } catch (error) {
                    showError("Could not delete daily note", errorText(error));
                }
            },
        },
    ];

    if (hasNote) items.push({ separator: true });

    items.push({
        label: "Copy date",
        action: () => copyText(isoDate(date)),
    });

    showContextMenu(x, y, items);
}

async function copyText(text) {
    try {
        await navigator.clipboard.writeText(text);
    } catch (error) {
        const area = document.createElement("textarea");
        area.value = text;
        area.style.position = "fixed";
        area.style.opacity = "0";
        document.body.append(area);
        area.select();
        document.execCommand("copy");
        area.remove();
    }
}

/* =====================================================================
 * Context menu and menu bar
 * ===================================================================== */

const contextMenuElement = () => document.getElementById("context-menu");

function showContextMenu(x, y, items) {
    const menu = contextMenuElement();
    menu.replaceChildren();

    for (const item of items) {
        if (item.separator) {
            const separator = document.createElement("div");
            separator.className = "menu-separator";
            menu.append(separator);
            continue;
        }

        const button = document.createElement("button");
        button.type = "button";
        button.className = "menu-item";
        button.textContent = item.label;
        button.disabled = !!item.disabled;
        button.setAttribute("role", "menuitem");

        if (!item.disabled) {
            button.addEventListener("click", (event) => {
                event.stopPropagation();
                hideContextMenu();
                Promise.resolve()
                    .then(item.action)
                    .catch((error) => showError("Action failed", errorText(error)));
            });
        }

        menu.append(button);
    }

    menu.classList.remove("hidden");
    const rect = menu.getBoundingClientRect();
    const left = clamp(x, 4, Math.max(4, window.innerWidth - rect.width - 4));
    const top = clamp(y, 4, Math.max(4, window.innerHeight - rect.height - 4));
    menu.style.left = `${left}px`;
    menu.style.top = `${top}px`;
}

function hideContextMenu() {
    const menu = contextMenuElement();
    menu.classList.add("hidden");
    menu.replaceChildren();
}

function closeMenus() {
    document.querySelectorAll(".menu.open").forEach((menu) => menu.classList.remove("open"));
}

function initMenus() {
    document.querySelectorAll(".menu").forEach((menu) => {
        const title = menu.querySelector(".menu-title");
        title.addEventListener("click", (event) => {
            event.stopPropagation();
            const wasOpen = menu.classList.contains("open");
            closeMenus();
            if (!wasOpen) menu.classList.add("open");
        });

        menu.addEventListener("mouseenter", () => {
            if (document.querySelector(".menu.open")) {
                closeMenus();
                menu.classList.add("open");
            }
        });
    });

    document.getElementById("menu-exit").addEventListener("click", () => {
        void exitApplication();
    });

    document.getElementById("menu-about").addEventListener("click", () => {
        closeMenus();
        showAbout();
    });
}

async function exitApplication() {
    closeMenus();
    try {
        await queueSave();
    } catch (error) {
        // Best effort on exit, matching the Java application.
    }
    await invoke("exit_app");
}

/* =====================================================================
 * Modals
 * ===================================================================== */

let modalResolve = null;
let modalCancelValue = null;

function openModal(options) {
    const overlay = document.getElementById("modal-overlay");
    const titleElement = document.getElementById("modal-title");
    const bodyElement = document.getElementById("modal-body");
    const buttonsElement = document.getElementById("modal-buttons");

    if (modalResolve) {
        const previous = modalResolve;
        modalResolve = null;
        previous(modalCancelValue);
    }

    return new Promise((resolve) => {
        modalResolve = resolve;
        modalCancelValue = options.cancelValue === undefined ? null : options.cancelValue;

        titleElement.textContent = options.title || "";
        bodyElement.replaceChildren();

        if (options.body instanceof Node) {
            bodyElement.append(options.body);
        } else if (typeof options.body === "string") {
            const text = document.createElement("div");
            text.textContent = options.body;
            bodyElement.append(text);
        }

        buttonsElement.replaceChildren();
        for (const definition of options.buttons || []) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "modal-button" + (definition.primary ? " primary" : "");
            button.textContent = definition.label;
            button.addEventListener("click", () => closeModal(definition.value));
            buttonsElement.append(button);
        }

        overlay.classList.remove("hidden");

        const focusTarget = options.focus
            || buttonsElement.querySelector(".primary")
            || buttonsElement.querySelector("button");
        if (focusTarget) {
            requestAnimationFrame(() => {
                focusTarget.focus();
                if (typeof focusTarget.select === "function") focusTarget.select();
            });
        }
    });
}

function closeModal(result) {
    const overlay = document.getElementById("modal-overlay");
    const bodyElement = document.getElementById("modal-body");
    const buttonsElement = document.getElementById("modal-buttons");

    overlay.classList.add("hidden");
    bodyElement.replaceChildren();
    buttonsElement.replaceChildren();

    const resolve = modalResolve;
    modalResolve = null;
    if (resolve) resolve(result);
}

function showAlert(title, message) {
    return openModal({
        title,
        body: message,
        buttons: [{ label: "OK", value: "ok", primary: true }],
        cancelValue: "ok",
    });
}

function showError(title, message) {
    return showAlert(title, message || "Unknown error");
}

function showConfirm(message) {
    return openModal({
        title: message,
        buttons: [
            { label: "Yes", value: "yes", primary: true },
            { label: "No", value: "no" },
            { label: "Cancel", value: "cancel" },
        ],
        cancelValue: "cancel",
    });
}

function showPrompt(message, defaultValue) {
    const input = document.createElement("input");
    input.type = "text";
    input.value = defaultValue == null ? "" : defaultValue;

    input.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            closeModal("ok");
        }
    });

    return openModal({
        title: message,
        body: input,
        buttons: [
            { label: "Cancel", value: null },
            { label: "OK", value: "ok", primary: true },
        ],
        cancelValue: null,
        focus: input,
    }).then((result) => (result === "ok" ? input.value : null));
}

function showAbout() {
    const dataDirectory = state.config ? state.config.rootPath : "~/arbonote_data";
    void showAlert(
        "About ArboNotes",
        "ArboNotes v1.0.0\n\n"
            + "A hierarchical note-taking application with\n"
            + "rich text editing and calendar view.\n\n"
            + `Data directory:\n${dataDirectory}\n\n`
            + "Rebuilt in Rust with Tauri, Quill.js and highlight.js.",
    );
}

/* =====================================================================
 * Splitters
 * ===================================================================== */

function initSplitters() {
    const workspace = document.getElementById("workspace");
    const leftPanel = document.getElementById("left-panel");
    const mainSplitter = document.getElementById("main-splitter");

    let resizingMain = false;
    mainSplitter.addEventListener("pointerdown", (event) => {
        resizingMain = true;
        mainSplitter.setPointerCapture(event.pointerId);
        document.body.classList.add("dragging-splitter");
        event.preventDefault();
    });
    mainSplitter.addEventListener("pointermove", (event) => {
        if (!resizingMain) return;
        const rect = workspace.getBoundingClientRect();
        const ratio = clamp((event.clientX - rect.left) / rect.width, 0.12, 0.7);
        leftPanel.style.flex = `0 0 ${ratio * 100}%`;
    });
    const stopMain = () => {
        resizingMain = false;
        document.body.classList.remove("dragging-splitter");
    };
    mainSplitter.addEventListener("pointerup", stopMain);
    mainSplitter.addEventListener("pointercancel", stopMain);

    const treePane = document.getElementById("tree-pane");
    const leftSplitter = document.getElementById("left-splitter");

    let resizingLeft = false;
    leftSplitter.addEventListener("pointerdown", (event) => {
        resizingLeft = true;
        leftSplitter.setPointerCapture(event.pointerId);
        document.body.classList.add("dragging-splitter");
        event.preventDefault();
    });
    leftSplitter.addEventListener("pointermove", (event) => {
        if (!resizingLeft) return;
        const rect = leftPanel.getBoundingClientRect();
        const ratio = clamp((event.clientY - rect.top) / rect.height, 0.2, 0.85);
        treePane.style.flex = `0 0 ${ratio * 100}%`;
    });
    const stopLeft = () => {
        resizingLeft = false;
        document.body.classList.remove("dragging-splitter");
    };
    leftSplitter.addEventListener("pointerup", stopLeft);
    leftSplitter.addEventListener("pointercancel", stopLeft);
}

/* =====================================================================
 * Global handlers and startup
 * ===================================================================== */

function initCloseHandler() {
    const eventApi = window.__TAURI__ && window.__TAURI__.event;
    if (!eventApi || typeof eventApi.listen !== "function") return;

    eventApi.listen("request-close", async () => {
        try {
            await queueSave();
        } catch (error) {
            // Best effort: the Rust side has a fallback timeout for closing.
        }
        await invoke("exit_app");
    });
}

function initGlobalHandlers() {
    document.addEventListener("click", () => {
        hideContextMenu();
        closeMenus();
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            const overlay = document.getElementById("modal-overlay");
            if (!overlay.classList.contains("hidden")) {
                event.preventDefault();
                closeModal(modalCancelValue);
                return;
            }
            hideContextMenu();
            closeMenus();
            return;
        }

        if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "s") {
            event.preventDefault();
            queueSave();
        }
    });

    window.addEventListener("blur", () => {
        if (autosaveTimer) {
            clearTimeout(autosaveTimer);
            autosaveTimer = null;
            queueSave();
        }
    });
}

async function init() {
    initQuill();
    initMenus();
    initSplitters();
    initGlobalHandlers();
    initCloseHandler();

    document.getElementById("cal-prev-year").addEventListener("click", () => shiftCalendar(-12));
    document.getElementById("cal-prev-month").addEventListener("click", () => shiftCalendar(-1));
    document.getElementById("cal-next-month").addEventListener("click", () => shiftCalendar(1));
    document.getElementById("cal-next-year").addEventListener("click", () => shiftCalendar(12));
    document.getElementById("cal-today").addEventListener("click", () => {
        const today = new Date();
        state.calendar.selected = new Date(today.getFullYear(), today.getMonth(), today.getDate());
        state.calendar.year = today.getFullYear();
        state.calendar.month = today.getMonth();
        void refreshCalendar();
    });

    try {
        state.config = await invoke("get_config");
        state.tree = await invoke("load_tree");
        assignIds(state.tree);
        state.expanded.add(state.tree._id);
        renderTree();
        initCalendar();
        await selectNode(state.tree);
    } catch (error) {
        showError("Could not start ArboNotes", errorText(error));
    }
}

document.addEventListener("DOMContentLoaded", () => {
    void init();
});
