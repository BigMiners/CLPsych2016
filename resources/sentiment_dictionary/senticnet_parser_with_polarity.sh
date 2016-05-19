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
		unset list
	fi
	#if line is associated to the concept, we store it in a list
	if [[ $line == "<semantics xmlns=\"http://sentic.net/api"* ]]
	then		
		relatedw=`echo ${line%$"\"/>"}`
		relatedw=$(echo $relatedw | sed s/\"/\\n/g )
		relatedw=`basename ${relatedw##*:}`
		list=`echo $list $relatedw`

	fi
	#getting the concept polarity and adding all lines to the dictionary
	if [[ $line == "<polarity xmlns=\"http://sentic.net/api"* ]]
	then			
		polarity=`echo $line|sed 's/<\/\?[^>]\+>//g'`
		for i in $list; do
			word=`echo $i| sed -e 's/_/ /g'`
			echo -e "Adding " $concept' \t '$word' \t '$polarity
			echo -e $concept' \t '$word' \t '$polarity>> $2
		done
	fi
done
ENDTIME=$(date +%s)
echo "Task completed in "`expr $ENDTIME - $STARTTIME`"s, your data is stored in "$2"."
