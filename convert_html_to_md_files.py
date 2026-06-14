
import os
from markdownify import markdownify as md

for dirpath, dnames, fnames in os.walk("./arbonotes"):
    for file_name in fnames:
        if file_name.endswith(".html"):
            with open(os.path.join(dirpath, file_name), "r") as file:
                file_content = file.read()
                new_file_content = md(file_content)

                new_file_name = file_name.replace(".html", ".md")
                with open(os.path.join(dirpath, file_name), "w") as new_file:
                    new_file.write(new_file_content)
                    new_file.close()