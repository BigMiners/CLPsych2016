#!/bin/bash
STARTTIME=$(date +%s)
#if wrong number of argument
if [ "$#" -lt 2 ]; then
	echo "Usage: "$0" source_file output_file "
	exit
fi

#test if source file exists
if [[ ! -f $1 ]] ; then
	echo 'File "'$1'" is not there, aborting.'
	exit
fi

#test if source file is empty
if [[ ! -s $1 ]] ; then
	echo 'File "'$1'" is empty, aborting.'
	exit
fi

# remove existing output file
if [ -f $2 ] ; then
	echo 'File "'$2'" is already exist, deleting.'
	rm $2
fi

cat $1 | while read line
do 	#if line = concept, store it 
	if [[ $line == "<text xmlns=\"http://sentic.net/api\">"* ]]
	then			
		concept=`echo $line|sed 's/<\/\?[^>]\+>//g'`
	fi
	#if line is associated to the concept, we add it to the dictionary
	if [[ $line == "<semantics xmlns=\"http://sentic.net/api"* ]]
	then		
		relatedw=`echo ${line%$"\"/>"}`
		relatedw=$(echo $relatedw | sed s/\"/\\n/g )
		echo -e "Adding " $concept' \t '`basename ${relatedw##*:}| sed -e 's/_/ /g'`
		echo -e $concept' \t '`basename ${relatedw##*:}| sed -e 's/_/ /g'` >> $2
	fi
done
ENDTIME=$(date +%s)
echo "Task completed in "`expr $ENDTIME - $STARTTIME`"s, your data is stored in "$2"."

