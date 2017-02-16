
for file in ./target/*.jar
do
    if [[ -f $file ]]; then
        #echo $file
	exec java -jar $file -d
    fi
done
