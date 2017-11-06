cd $(dirname "$0")
files="target/*.jar"

file_found=0

for file in $files
do
    if [[ -f $file ]]; then
        #echo $file
	exec java -jar $file -d
	file_found=1
	exit 0
    fi
done

[[ $file_found -eq 0 ]] && echo "Error, no jar files found!"
exit 1
