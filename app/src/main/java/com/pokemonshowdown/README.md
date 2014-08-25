Get prefix to all files in window:
for %I in (icon/*) do copy "icon/%~nxI" "iconfixed/prefix_%~nxI"

To get all file to lower case:
for /f "Token=*" %f in ('dir /l/b/a-d') do (rename "%f" "%f")

Or just use Bulk Rename Utility