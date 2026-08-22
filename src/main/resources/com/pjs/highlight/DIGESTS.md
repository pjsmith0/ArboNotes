## Subresource Integrity

If you are loading Highlight.js via CDN you may wish to use [Subresource Integrity](https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity) to guarantee that you are using a legimitate build of the library.

To do this you simply need to add the `integrity` attribute for each JavaScript file you download via CDN. These digests are used by the browser to confirm the files downloaded have not been modified.

```html
<script
  src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.12.0/highlight.min.js"
  integrity="sha384-KnPvYPx1poT554tHDV1nuYV9sOkh4cZPBvLZQlXgJmoRQZPdgQNwL50/xq9kynp9"></script>
<!-- including any other grammars you might need to load -->
<script
  src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.12.0/languages/go.min.js"
  integrity="sha384-orYKHAs3chK3oDMQLy5ywrzoY8z9zvzfmNIjmVxKXioAUtwDhP+xf6THWYSI/43Y"></script>
```

The full list of digests for every file can be found below.

### Digests

```
sha384-LSTslyR9aWBOJ9McYIgxItpMeyUofwMJzSuove0l6R2eha2RQT2Q5AMJVs4V7m0P /es/languages/accesslog.js
sha384-NTjHdCNUmuk5couSbJgpHbdybg+wIpQg5kjcGQzSKsltpo9MKGiOqTOgOsEBBqmh /es/languages/accesslog.min.js
sha384-tS2E6fEhoQP1YJZ1bC4syfJ/n3rN2kRIW4swwHXcjuyk2pKSz2a5ylP7LpWGVuk4 /es/languages/apache.js
sha384-1tpiYMU/lPlP2J8rSF/nTKRkEnwY3V3ojcYZqRvOZOH92XReC4gtl2EaGUDj8Ufw /es/languages/apache.min.js
sha384-Vqe1AEoiNcLtV2DfIX2ddu9YnDvXbzuMi4hx542iG7/t9WlE1sobqt9DHidv8/23 /es/languages/bash.js
sha384-8FqiBkFvhBoCq7xHJ8pB0H0/hBH2FJ0IHl2CcZWpINYY6uyw38RJgZNe5fddwEaa /es/languages/bash.min.js
sha384-A3L5Tf7tUTEvvb7CBQ7/Ss1TL493yDjIre9lL9+hewzsmlGvDaF0zr2jtRL/as9+ /es/languages/c.js
sha384-QjLTklOpGRj5BiUGiD7/7RYVgazp8V+oQvLPmM+goByoChSDDxbVf7ztjZ5MtHO2 /es/languages/c.min.js
sha384-Lm068HVDQtM6SZyXhRJXHaa0hWobIT2uMLFN8u7PNsmbDstY7X6RUJKI8acvPv8u /es/languages/cmake.js
sha384-KwzHwJUeSzuUbMpf1pgoYz9m9Ds2sQw62RB4FLaF016mSHMJXp02hqQSa7q9/UsS /es/languages/cmake.min.js
sha384-jJSKb0kbVcDJLL6D9Zc80g+XBwa84Ogoi6JH0dZMQJlbzn6ZCjYFE2WtU6oTc8Au /es/languages/cpp.js
sha384-p1ClIV57O7/nb8vDAVFIhZ7u0/8oonjMxc4Yd8EarzxmLPMiQAV95I0ogb+ZkEFN /es/languages/cpp.min.js
sha384-RXftkXoNvYqUJwOt6SHqM2LYeZbYCBjM5NavsO90nFvSyfeabXOwVuiQQPK9IiXG /es/languages/csharp.js
sha384-/Y02okSZEfrz8yFmiDDj3/KtF/vixqlddULDmRbQpm3mzodelRMVW9a02TKwR6XI /es/languages/csharp.min.js
sha384-amdMjFrQeV1IlGyVyYRGeUBxPp1NVz7WG5xs0heAwCiAZLj0ISxeJwiOTeom9RfS /es/languages/css.js
sha384-rLeEizUP6J+98gF7EZ4ngav3h+slU5SqCVDahqOoYBEdjzhWQ3g6XldnqR9BSlBR /es/languages/css.min.js
sha384-YZV+5Xsuvo6Q4cZqzweOIYyP/d/r3hzQ72jXW8DgxloD9kGZ7RVcMBU2cdqj7A3p /es/languages/diff.js
sha384-/phJKlZdq/5z9S9Vt/0FmVNJo+BL+l//pybsa2JDkdOqAgadem2/HjHddt/T0kfm /es/languages/diff.min.js
sha384-dljpS1l9+O5vQxJissODJYNA0Rw9nJp43VsjEb6FyGD6B46ZBK1W1RfyGqeHMva4 /es/languages/dockerfile.js
sha384-kbkyUe6C+7Rb+UHlnDueefZESgm3YEG737vAbnSDnao1ovpiHTZAGhs0bvCyDXS8 /es/languages/dockerfile.min.js
sha384-szxUUh9N0l6P/FmAIblkcuHCevD/uQZvxzRV+3/cXGUfwkGB1lUermC0svmtpDLw /es/languages/go.js
sha384-r/ODTWqgPwC7it6O0gCXlszLaDESga+Z9PaVcIfITv1CfJ39NpFiz72b2KGWbFBQ /es/languages/go.min.js
sha384-EwVd3CFnuRZREukld14ypTfnD1MGYSHObt198ZAyUlU7VqS8G3X1QqytSOrAFc1t /es/languages/gradle.js
sha384-njt8v8LhCccOAGtpj7W5Vfec/jKsj8Iwks6Ncz+WS/zkJXZdOJCwPpoLSXv81XdB /es/languages/gradle.min.js
sha384-/7UhdV8HWyDNMjjBiJcQHmfrTIWMpVWR2U1tJX1W1akkq3nl2fw453Dc05nGPs6w /es/languages/graphql.js
sha384-wyUy1EGF0XgzqS6l/2/qf1UjLsiLXFSKgiWRx6llD/6WOrug9aviny5bkO0pMo44 /es/languages/graphql.min.js
sha384-hzQ2yj1Qgn9QFeoeiOxlecYge16nDa0kYU3K/iQ5+2Y0JelkS2+LEgu86rHjA/yO /es/languages/http.js
sha384-sVSw9OMM4hyWaJ7X1x8cNgeubs4kjR9jfW1hab3Nh6STAKRdw7yBtaVRnSeVFOCH /es/languages/http.min.js
sha384-o4UvWNmnl1L+ChjYSiEzv6+SmutMhWyMN6YBulDKvTe8IUDetXNVFtu3jjjUcEy6 /es/languages/ini.js
sha384-om1+Rx6vks+orizrPIf1OpBFEOqHVfjPlx6hSoIY80V1fDLkPa69GFlafLtsMSWQ /es/languages/ini.min.js
sha384-z9bZmXQ6WniHUvySgc0/iokrwX54kIx/ZDvUIHIwAjgHRFjLZgNKiTX3UBi5aY/4 /es/languages/java.js
sha384-WmZNtzcna3shPin4x0B8fSeXgtW14zoyha+gmtNTU53pa/ZImw0O8FmX0x06ETq+ /es/languages/java.min.js
sha384-mxaIAuwA1l6te9LMbWwt9PNtaoRiwRk1/345TMC2UQtNTi1kjbhizCrSxaHAegHF /es/languages/javascript.js
sha384-r8C5XKdITWu1xHcHMIfmqgbWZTa0w/MPyAykL+WctwUoeTsEHBo5+jSSoHQ+qFy6 /es/languages/javascript.min.js
sha384-lt0gg86v1uEAI7/c2402dN+9T2iQXopS6OJ8P1jHo1uOZu1zIV5sX8vmlOLGozq0 /es/languages/json.js
sha384-UJwfLbfKiYs+crNOV1xJL6wDet7JiH/Kav6qZ9c+cOjar4piP1X0VQqwVE3RJt+z /es/languages/json.min.js
sha384-vezqgWMJbGAsYZJt0Y8PzfZGwJxP0Y/cRK4t0IOj7vezG9UyzImQtWaf1eGDa3gp /es/languages/kotlin.js
sha384-tgcG0c1H00f3oMBpvE0wMu4d/qhALhNYL5bXrVFQLy4tJuPfgmBQCLv74Yi1qi5r /es/languages/kotlin.min.js
sha384-bDh/jrDlM0OSA6LIT2wBxy50zFGhtLxAZ+iJONX+uwjJqYld7tF+mhbOF27H9Z0z /es/languages/less.js
sha384-vs76c5ilg0Vaa9O2Qj6tXlllVDVRb2RFmGDLV2esNIN+7VVbwi+MDZwkvDHs3qsl /es/languages/less.min.js
sha384-47Dn27oZJHoNVUL4leYvOLfxpXtmetYh9lZ7olKoDqzPebdZqqLE3xNY9iHEu3O/ /es/languages/lua.js
sha384-RUO5JMeXmbOe7bDSAF/Yzw4V9xXupHGlmNKK4+/UZIPdock3BGdBwaWYzM+j5se2 /es/languages/lua.min.js
sha384-btFSo3SEJNPWl4YEj99Ws4vfJOENZoIppHk/IDH2iGwIWHWA9e/DuDjj7sSQJH12 /es/languages/makefile.js
sha384-ai0V0qlpdcBs1bZEDPT9mIIFwPXiKIjVtDrDZL68xKan6TWmMxuzMKycOP42aBId /es/languages/makefile.min.js
sha384-qXAmSIiI587+IHpd/KU7WRz9snrtBhVCR5pST316BxhuxRbXzb6uWmB8WC/2eqUi /es/languages/markdown.js
sha384-atzUeXgldTnp0Vvir5hFSZ4MRX0FWOpxp7UdXca7uje0uL5S6swwfRhq5SB5Ag0m /es/languages/markdown.min.js
sha384-LVI73PJyetgdH5xfoLHnfMG1XNBZ87+gKKX7F7a3RH61tK83W6eDiT1MXY6XAu4+ /es/languages/nginx.js
sha384-Tb5P4iavWHq40th7BFBMLnIj7GD0MxwxfRvAoa4GFd/wqiL+7X5yYT0aabTTaxal /es/languages/nginx.min.js
sha384-QyPMh6a5b0314fQDFUNmrDEyXVTmB45XyomYlSsdqRY1tobFFFQoKv+MEnWzAXk3 /es/languages/objectivec.js
sha384-VFdwRNgJ03w/CRW3VH62k2PtuzjauNiqidH19xedJihmf+WpBJCdupSTAQDECifU /es/languages/objectivec.min.js
sha384-A3z4/rENhAxuUQZt0VxjOtTyag39qTgPj8b81lFacYauRFxnQn6dCcmyT5vosPU2 /es/languages/perl.js
sha384-DOsDuQxEtE0yMCwfPmbFug8IZmHhEsZl7L8sCwxmdIiaKYXpU7MHZtYD0WrGSjOA /es/languages/perl.min.js
sha384-XTeMzDnDAArjgJ23hiCsxPaPHpBkHKKkFjMeF4+pJypfe1xnCjzCMLTDOVIxbR6+ /es/languages/pgsql.js
sha384-/Kdh96MsaQHxUcgLlPColFSwhmYLUwv1qxfakzlLRp1giw7DU6gTEI53+fQss6r+ /es/languages/pgsql.min.js
sha384-a7CJHRB29/7Hhj+fm2T+rVAfyTTsDBmFA7Jyb8gWyA/IX71UDkK0TBanG+K4T75i /es/languages/php.js
sha384-HmKbGhJX2Y98LBeAFLqI79rljxMw9FbDEbbWQfYo3vTeMjCLncFCwSrdgegOv9go /es/languages/php.min.js
sha384-QaxNZpHYAAzLzhLDx18qU4vEDgn+flgAbw801FFTpFd8ydBayxNUUdbfV0Ngi/kI /es/languages/php-template.js
sha384-R/QPLXCkbFr59NTgGW3+UOcdf5QG2KBRd1biV3ihxsd5AxSVl2VE//IUNhg35Kqh /es/languages/php-template.min.js
sha384-UbOS4JomBdQgpHT9lwmZipdtIqKiJP8T2IG7oG0l6K/LnyVrSMoaPI7Hh6vWatoS /es/languages/plaintext.js
sha384-RhTHpYmm2hh4ZEkHCbk+tklVV1oUvBVYUfdesSGg3QO/JtYDmqYoycxlBJxg9Lqb /es/languages/plaintext.min.js
sha384-mZvq9KdeRtohLsF3zEec4vwiXvjryRuTGzduyudrung/CRXN81fRxAuVxjJX/QVY /es/languages/properties.js
sha384-NG0pliBFx+fuWPX9uEIzhRyZ51y/VdYZoKlhGFrw50SHI+r/EXMqrbuBcfNMRHuf /es/languages/properties.min.js
sha384-M29KVshhb1nOr+V9fD7zculEkusgFmZdZpLoQxEiEJ7nTncEiXzJ9/tXuofiOIYK /es/languages/python.js
sha384-1SN2ySEZrgpb2XQB4PRJli/u1Jj5sm1j+Be77jnSNNmKzhUZK8OBkGFFexuOPSHu /es/languages/python.min.js
sha384-eOXRcD62va+i9VlDYecRvoMjuDY7GltQDo4PJM4ylUsN49CCqXu+fumqfopj7Lbi /es/languages/python-repl.js
sha384-L9i6yG+cVyCdxP1Bu+OO1lfIT+A+6bU5xgNYBtvY7AhRK7eOLvwnxXS696wxFZVR /es/languages/python-repl.min.js
sha384-RdcmcN+/XvSqKljXtkqUJHBlUAT8ndQI+w5IhKHuioHvZs+m8sMRxV5JihKPWgV7 /es/languages/r.js
sha384-pzlBoVr78luNmZ2t7oTrDsAWnaoDLVO/ZQBEYBuN5dR2QLH0oldQCd4Wjn64HJxH /es/languages/r.min.js
sha384-ApNDxgmZnH1HTziUAgsU7UkaKsn+3e5HyrIbzOalWXhB5lN926Km2GrfrJmJOq5o /es/languages/ruby.js
sha384-AUyGIa32efsNJIJnJtL4/+5aHud13BPHq88Q1QqvIhXYcnCMgJTmvq64W4LuD1Dv /es/languages/ruby.min.js
sha384-qiJr0S5VCc1t8759xUObwojkNSGY9qsZnhF4WrRMssQm/vr/af5856XqXCpUyAVX /es/languages/rust.js
sha384-GGN+S5ob1CnW7D1agsXyjfA+lWHjZh4PXNcFl0in3hHcusBs9NgYuIvQkeK7ylxn /es/languages/rust.min.js
sha384-KIISJ1MIG4sJ5EmeehDSTxWDQLEV3xogIXFtI39eXo4DjSvbAIm6s6m0Ckw5OsxB /es/languages/scss.js
sha384-qTIHQ6M1cS8rvFci6dL2FprsPEsvR7KlWL271KM5K76FwC6VVGHaDL0HP4/YDzQA /es/languages/scss.min.js
sha384-B2tWwx3GmC07EO8LzXOdP8ST25VZHxugHG+XTI4uPSvRGWHnTPKEWip5WweN7GnW /es/languages/shell.js
sha384-UO9hFSZLgfmMtuKIFmLhOz4OMlYKPDnZyxHVmn47vrmiTA6MqC2D/Y5rdVO49Alf /es/languages/shell.min.js
sha384-1x+arn/A8CSZOUs83+Fa6bwOOwxzz9Fqc+OZ3YVh+yVOvF5H6uvqHDx6Bh4UNAp2 /es/languages/sql.js
sha384-e1St/oZyx5GxD71Zry3asHLIZmg/b20NgNJLUwvput4g+SZj8Rjuq+aP7pdWC2qh /es/languages/sql.min.js
sha384-N4p1ha5k4CtCzQcGVUCnJIExAIOcg1I459dN2Ji7oxtWzdPRPFKB/0w01ice9b9H /es/languages/typescript.js
sha384-h0Era/sa5FCAtyCMoAbbbX9lB98KRHNuiO6RNIuiyOP8uOQ7YU9vNT7iVm9SP47K /es/languages/typescript.min.js
sha384-7a2bpMnPJYsfSKozTYCMa18xjuZiBhhDNHQ9UWAE0HaxEIrzquRliVJhUEj7tZ5b /es/languages/vbnet.js
sha384-uL0SP9QQbkReeTk/LOHcJor2MHps1k/3ut2x/oYiRYGyBPZnUlx1V29F7zWhklBm /es/languages/vbnet.min.js
sha384-ycAPZa5IlXuBe8oF85Mtjd7de5YzS1vd+dObxI93AzWb9vQlkuarjn4QZuF8y9ac /es/languages/wasm.js
sha384-35DLnwpz8NQs6qIwD2V3HdsDplcA1ie+RnlRwzYAJ4HJwMBw6njtU+p2+6Q8hl5/ /es/languages/wasm.min.js
sha384-XZNCXUeNSjWoW5lAESpD8AkU5NhwkwL0a6wIzJWfMEx6qNtF29L+81oxGOy4b3Pj /es/languages/xml.js
sha384-7lgbaoMNJXxrndTFyw0ll0hq1MZzDLFkFmLhYLibSJNXgcW6xOSU9e+OS2QaAKDP /es/languages/xml.min.js
sha384-V4dEHxGPcfKe0nPj1Kf4bHhhEWQok5V5odOaTC9ADMs0bJqyVuFfVDkhTZaSWwpC /es/languages/yaml.js
sha384-nw7e1KnZnvc0mX9u7q45N8KXp4CIDO3+GsbJgpVp4Ye3b1taSPCD2+dtUlqqjTYC /es/languages/yaml.min.js
sha384-xQPqH/KGz16LPm1lt+2ISaK4NhzbVvOci97TePQreYKrHYXIpjbE89WX3ua7nsH+ /es/languages/zephir.js
sha384-+odJ5CU1wNSJ17885QGjEDKOwEHBgQF6q+Zaihycuu4zpyW+6AK3GWgh8hiZr7jn /es/languages/zephir.min.js
sha384-IOkpD/g5z1K6CEDxKL70IpqIrxyGyhqV9A/hncwdMdMOJ6WzKGj8U+VayG3jLS3g /languages/accesslog.js
sha384-bG/6dXLz6Su49NXLKaZ6gCNHJLZEYJCBC6L2uFC7FymcYiXa+mWaHT5f5niJWf2J /languages/accesslog.min.js
sha384-JPdWuV4rMxurE6OTscEtwT5/tCUeJ14PlrvcgAP32DcPqErA0/lvrLmqpteBCYJq /languages/apache.js
sha384-QraNZk4EDfTVeWmClFLg5BZNVnDF8kF6L0wgGHxg201ohjeXkPbNzC5dzdpjxdnB /languages/apache.min.js
sha384-5DAFEjB/sJxmunBVtQT5pIOWPcB8/sbocTrB4vhqxJ3G38RVlxXiUInHwkdik75C /languages/bash.js
sha384-3LwgJG25r+ir+HS3WNVBMDCbzxie+2TxFfg/kMUMmtWF9mxd0dEDwFS2LGd8akPz /languages/bash.min.js
sha384-++jr2IZH8qZUHclpwLKPdPB67oloAwlp6bVn6IOBm1PRyrATGwIJDemLwIjTXWhI /languages/c.js
sha384-XUlYYDNkxUkC5F86/mGCoT9eEdPUigCR+8ihyrqFat6tMD0/GRHkorEtHKgBSS7D /languages/c.min.js
sha384-N7VBaChBo0g9ipud8RC9FW1fMn5BhxpR7Y8I4d1H493YWprFyC/Z6g8A4IIp6Vq0 /languages/cmake.js
sha384-2+d+VUsqaxDE3yoeRJxqWmX1dF5Ongwq0zKRb+NmBsIQXlRr0GTQOkDDD6eujd4k /languages/cmake.min.js
sha384-Fu6emj6YokTl+GgIHZRr4tqDnAWVGmEP67A0XguFYxkNQLhiKEgZa3J2t21BQ7Fq /languages/cpp.js
sha384-csK05VfEiSG0s/zJz067fB5PZCE5+Uo1yqgoa1zZkSG1cmBaOuXEGSbVr2YqAghu /languages/cpp.min.js
sha384-QYHS+ofe4eSYS3zcwW+oElJPLA2HYu4aYzmzXOpHsGwaM19ytDaqjbRzE0odwrtF /languages/csharp.js
sha384-UmTmCSuac/JgV+xJdKhzfTJ6R7AIX7P48kgC024cGCbJSx6vePS4XPyMrzU5LW/v /languages/csharp.min.js
sha384-+G97Y66qjmfAEeNK5AYrOqbLn/hBNX41qhtyiVW7z3Zq/1llyjGJr3gHmNi+AVKN /languages/css.js
sha384-FvHR2wIZNmDX0TgSuoOhAZRl6R5yRi26wu2/MVXDm1ZFCGJUvotj2RrvVLGC4y88 /languages/css.min.js
sha384-ckUuyBRUKxbDZUJ4H4noBUDEnHQ+yGzjV3C/obGbXYNU2HDcsUd5DY5dGhCayqnT /languages/diff.js
sha384-1Kh9Pt8vmw11C0wtJL3lFNQiv+gHJd/yQM2kMRT8CkYpRa37Q37i05myTeYhaUqG /languages/diff.min.js
sha384-4gOC/347d+9AQct8q3iBSJ1eT4EWYj4IGYX58+vwZq9bzbi204n6ZPly0NSRxh2P /languages/dockerfile.js
sha384-/zu1pI8+9j/v/qNlCRRyidiBhGdxfvGwOLXEPBXpKc77eFNUAhccr0WglEQ+x9La /languages/dockerfile.min.js
sha384-JB+Is5MW6WOc+0g9Rtdr5MJ5DBFhkGV72i5St11vYSu28mXrGGOLA76MQT9sKafD /languages/go.js
sha384-orYKHAs3chK3oDMQLy5ywrzoY8z9zvzfmNIjmVxKXioAUtwDhP+xf6THWYSI/43Y /languages/go.min.js
sha384-LnbojqEHqgoG5/3NNwF/zy0I3nPolpEBr0mCo7bA8oiuLCF7k42Z+e6dQTWTF5nR /languages/gradle.js
sha384-ZMf1TkmgwGuFpXWyanMnGiBQS/hdqUrJkNZfPm3GZ/CGypwNzesT3EX03iY30WN5 /languages/gradle.min.js
sha384-6+lopWGnw1vvdbqZGaN2KQ2tE8gE1mna8C4DwWgw5Fgj31XtSaTYZMByQx0LRtRr /languages/graphql.js
sha384-zbJtnmNvDsWQBDyuEcOlAHWtjQrX9yAyms89GG2HBTXfka/v+nzfopvruqYG/F2D /languages/graphql.min.js
sha384-zmInyL/uNPOnQvVfQtEAadsrA71Cn2otNpIRZB5XZznH8p6RQb5UaYxSToivYtM3 /languages/http.js
sha384-L0lAPMci4/TxXazgm0dJIgX9udJzIJf78sk4o9v5sYuRVYJSjymFXF13LmyCU3L0 /languages/http.min.js
sha384-0tQ9l/wLu1PEfcESn4J1X2XjEGE4pKgn9SG3tfgEpRwX0yuZfCPtxSDQmjOgoO+b /languages/ini.js
sha384-FtUKo+hpWyLS/IYUX6GZGKptmt2igNPFsWSqPRz7qk7cnN+/sbt/22tz8lH2iEyk /languages/ini.min.js
sha384-Qj/fL9z0ymYPbd/2AWbWGyDfM3jjtdV4Vs4KdUS6OoIbA4AahIb3PpkR8nociDdi /languages/java.js
sha384-1OHpuM8WHOF2rIMbr6F9TXndkg39R1UtQGGfiifSXWlIthYEjIVkx94n05/gy931 /languages/java.min.js
sha384-5vRFHgNazcqNV/wYjVV73vv/mmcguTGfUhutWTMzUdixVclmxoe32uu3C1i5U+b3 /languages/javascript.js
sha384-luOC72UPK+5vw8AmdAZNVaFIY8IN7MayLzqcVcnUdCCVug/rAyhze5dpWklUZW8b /languages/javascript.min.js
sha384-xRs5pKapNPranWV1tpWwbWD8FN6u6gwlBXWwW+3wcfgCrXtc+VuTjt6Ff2MZTUoZ /languages/json.js
sha384-BuKQB2q4LIWSYzKqO0qkAQOdYLvqUmSmzUtLZDkTHy5po4tY7DSEcu/r5555QON2 /languages/json.min.js
sha384-yjC6cpJVSTSYxBbR4tcKLImOtDW6Kio9lMQmix74QqFLaNoxyKaSqxphExX9mqFM /languages/kotlin.js
sha384-bvTr+Eh8UHXQnrCLgKh7F2l4aYh0seHmbfJ4RtjNF+jLW3JzI0QuU+rTkeP7h6nR /languages/kotlin.min.js
sha384-ldbjDAIm+nFWhElZuPzQQPxffBSeej7snYvy8fb3pps6mGcqHEn1KPZGn6li62cn /languages/less.js
sha384-vtOZVRxEmiU1iIbY6AdrnW5QCRjdA1XwkG3TmGr8QvQvUvmBUdu8FzGkqde7xrjh /languages/less.min.js
sha384-X7kIOhO4b0cvc9Ro4AM1xtGHL0uSrOPcvdCaVRV4R2bp0WfZ+rwq0nRA+gcYmRsU /languages/lua.js
sha384-4O9XXj4HbJ3xc7QSKua6ZC/g7oB7lUP5VJo2Otc6LY62LJVfV/Ruod9z3NLR+W2R /languages/lua.min.js
sha384-InzoelUz5Kd5nr0UlmC3zkpZcx44OV88IrddbIg35m+umNK2EHFzQynfFBn34TOC /languages/makefile.js
sha384-B/EK9u4TdeCwE+1vigZs6g3/LKhzjAu8NNjRBbWox7nAgCTtEu5ctWCayQZD/SMr /languages/makefile.min.js
sha384-E5HEb58PlD3DyF1d/ePfVyiVVyWY78cCsbBBHPtlGSEFbNDHTcMoBnlt8dHP62OU /languages/markdown.js
sha384-VEGOCRct+04gpJrYhkaah46lXsjH+/u05lA+0IwU+eqzRpr2/bcRlnoHdu2xVr6+ /languages/markdown.min.js
sha384-Y1dc/AwEvPltbtXPtoSj5S1X+Y3ycTpP+siU9ANZnbWNZmU/0wmJWqebIWbUw6uA /languages/nginx.js
sha384-fTqzvE/t2lypa5UdKpG8P3qlAUy1xlle9ZtJj8r+9V9aRP/ePjUW7IWAHlU8phrY /languages/nginx.min.js
sha384-AgCsg3wazMuq9x9DcniwGFLCQ5W4nmwmARp5L7OqlZOfUNe32Sab4eopHe9cqGtB /languages/objectivec.js
sha384-v193e7sQet+Ed3D2B7eLo7Nb0mvIY/EWPdQzt1r/fJSSSteZZ1OlKnpFx6kQKckA /languages/objectivec.min.js
sha384-s3Sn07sJZKiVwp2wmDsXXm9k2HdJOswi9YBNlPyVcWiHUqh/aCvGaqVWRcmzfrJy /languages/perl.js
sha384-oPl9xVdjGiYsQCpX+uItZBkcyGL2+QR6u2WpsvSQp9YnSFBU4lGfHRnFCEtLlHN1 /languages/perl.min.js
sha384-sSDcfrmga/fptMUb3fYBzgdqGQVyfjQbdLqoQg3JAbZGzJSaDCRXgvLQWkd/5cxH /languages/pgsql.js
sha384-9rdIc+nt3LnsQUdyROz7e1pHsM27CXAhtRaGo0AhyHxGCsSCRLE64xeMNrpcASNy /languages/pgsql.min.js
sha384-KHE4nh2fKFHXTz70wPh2Q72wGbHIhBIXxhb0BhVPkWtlwAbUMFS3pSLw1c6P1THK /languages/php.js
sha384-67UUzIXiBnxtse2TXo+O7wMnXvIhxA+tqtSUpHiWsRJG3S7R+ORDGARRpw0xCcND /languages/php.min.js
sha384-27+7vrP/CrhVq/9tCEJ+rhJs0HBiL9xQhTLCsmPcg7xVW00wf0jykic8rjIToYjw /languages/php-template.js
sha384-CdaD7T+X1z3RvjQJYzg55PsdNz0THLyY/sYmQ4KWY72/WtoJL0+7ul7FFrMBq8Ru /languages/php-template.min.js
sha384-5antmxjEm+wiK5ytnS/IhTDt8w3EK4ELSuskQjN+4wPiEPP0PE6aE65JRI9R4ZVj /languages/plaintext.js
sha384-vpQpEp61iIf7WRxlpZKYck63VGy6819HP7jPmr8BoVQ1D6Ov7Z4RR9deMn/v/6t+ /languages/plaintext.min.js
sha384-QkfvvJzub2YubRX5KjcU4Wuyz/f1pQBjRSoY7azDfedKZnZ7+97cxwHv1Eum2IWl /languages/properties.js
sha384-xHbFPM8P0rCZ1js6aFBqwyDdUMK35/FEbVS6ZSdPtDjssqs17QJ2/M9kWJJUbm3D /languages/properties.min.js
sha384-vHFyigyrxXKrLcfzhSiSgjFvq0h4IaawkRiHBKD7g4xx6ny6hQvUeIe5o0vcr2Qi /languages/python.js
sha384-MjEDnJ45PjESeDQtPkC0gdCZlwA6MWEYLl/Qw95D9e1Ow8yAlz/xkS/EDIxFiig6 /languages/python.min.js
sha384-3TgqxwRPducb5/vGWjiLb4y0YJyGT8Z5mJ2kuoQDS+HuQgH16PHxiKupjgYSbM4c /languages/python-repl.js
sha384-2cTCVdOaOTLF7bNkHF1TF7PDz9YciUPV7pvo+IZaAjjY0r/Ph0medtLzbhZFAOnd /languages/python-repl.min.js
sha384-fFMR5EFIa6YPqEGnYTOztGy4PwYhgl2InEd1SOifkNjUrnY/FX1JO3vnz+YIP/tI /languages/r.js
sha384-x149hketIbY4jDkg82cxpbkuCJO+b+PunNrxHFuzTlkIMbEUflx7qOIwMU5kVNzO /languages/r.min.js
sha384-CNV85a+dSa4H9zFI9SB+/dBrwpbTiaUyTVrh1qnu3wnXCZJ9jk2lZqbLRugCpxeY /languages/ruby.js
sha384-3b0Nk4klTVUY3IaRgtyiENGDrNeXFvaKu5PUKyO6++pRcuFJpvwO4aZsvTycLi8D /languages/ruby.min.js
sha384-a31URvqct+K3nz3lO55mCXLo5aeM6yEpEgpPtcU5FnkQkEBn6SmHsPbJ5T0TF0ki /languages/rust.js
sha384-LscyYEhBL0qRGRT6l+Y2jHVM3FVaF42fVHmcY9cA9NL/Vs60Owkd0iOxjAzNuS8D /languages/rust.min.js
sha384-Pw/HuYa78ix1/THPAU1fbp4nN2tOT7inj88wIU0Y2HyhnwV6jigMIBNhzr+SLCVW /languages/scss.js
sha384-o1Gduq31NS1KqRP7OOK2ahBW/7+2WK3PxfaOnJoN12i6wpxzz0mc66xl+3IriZtw /languages/scss.min.js
sha384-72beB65rbLzMOcE4+YWKx5ru8/Ond638ZZwF6NlcoledcqrIWooTDsjNuAzieK1u /languages/shell.js
sha384-laOtV6vhnnfvegRPIJtGS2eVsVZ71Kh6gTxOoDhXpl91W1i6NYgtojnE+PFKWm0c /languages/shell.min.js
sha384-a+Iw5odyfpVyqnZLhXgjgfrTGL83SYa8BG2NpF/DmbvRloJefgDUZEDQ4j2662cz /languages/sql.js
sha384-xmw+Wgf/U9GjB60m9/I4SBh8zETMwAFC8GqSL7kCfkfgoKHDffjcnkQrNpQLc/96 /languages/sql.min.js
sha384-ZIObMe4G2tCNSeZmbuu/eNLQi+uWHn81EkG/LaLGRvHUzPfMORf03i0D+oBc/G0u /languages/typescript.js
sha384-s6+yoF8EeHfbf62mROdPSkW8JZRsupwEwDOPS8z4eH7V9s3T8QTYRzDgQumYraZE /languages/typescript.min.js
sha384-jCuEqlNZryNbuwbh982O9Agqf5Em/8Ax3Q8ZnkSRj1oAzkchyqwgaOfB4ZFF+Kct /languages/vbnet.js
sha384-p6syhCigzcASoNSw/iedrDiPhelCaP0rWmtm+AjXjGKwNzNmnFUDHedMwsxIuWAr /languages/vbnet.min.js
sha384-C6uMWEj1u4MYYqfg3yWmCuc/puaGvUNRVwydmM8P8Nqdv9GtvA2Cf/WOngl+slqa /languages/wasm.js
sha384-zzlOK8F5T1SrvUBWtv6dvphaE7hRZpYmKF9MA0rJKX0XLauyw+n60OjojiaKQCHp /languages/wasm.min.js
sha384-NNLNlM+AFtDXFlKRhmVO4gnfAl8a6U0y/QRO+jb8Cbx/1wYFSjpS8XF3mexCbQ7x /languages/xml.js
sha384-1iugfrw26YFHz7tD9aJCXklIDFXhf2qx4cJGfo4T8mWP+9nwOCnZpq0PqoXqcpO5 /languages/xml.min.js
sha384-QiM+6eBaRFHV4SXe9mvWNsiQXkInig0NsYzM6aIHbVEcgJ2j4WDRKSjUBTkqLUjg /languages/yaml.js
sha384-3Z1ACXMaXuAS5eP39k4q24JKpbbb3hxVfEYQuXhujNu4KZSYl/BepBcMrSys6psg /languages/yaml.min.js
sha384-IuJq1qpOD3nTL0oDaIdPcOhOelE55OkQ7dpTk907MITNO4J+iywbqmI5LHBA22/7 /languages/zephir.js
sha384-yYqQucQ02nhy4/ZBpTPz5h4/tonIYvtpNYL/9k8r5gueXol8ecDZGNRH6KqjEiIm /languages/zephir.min.js
sha384-3bKfi7UXbUq/n2SvvPHit4qEyRBsjgWhFN2Ccvi2SKHoYFQfm0jl9IZIJWeZHTpz /highlight.js
sha384-9nhXuc9J2Dat5zVPqr/m5K93fnMk2ioa0L0phqmBaGkxadpXOI+zIgFoz4lqyZts /highlight.min.js
```

