#!/bin/bash
## CLPsych2016
## script to sort CLPsych posts by label
## @halmeida, Feb2016 
##
## Usage:
## "Source files": path of all posts 
## "Label file": path to file containing {postID, label} (i.e., labels-training.tsv)
## "Labeled corpus": path to where labeled posts should be copied

## gathering directories
paths(){
  read -p "Source corpus [path]: " CORPUS_ROOT
  read -p "Label file [path]: " LABEL_FILE
  read -p "Labeled corpus [path]: " CORPUS_LABEL
  echo ""
  echo "=============================================================="
  echo "============= Source corpus: " $CORPUS_ROOT
  echo "================ Label file: " $LABEL_FILE
  echo "============ Labeled corpus: " $CORPUS_LABEL
  echo "=============================================================="
  echo ""
  read -p "Are the directories correct? [Y/n] " PATHCONF
  echo ""
}
## gather paths until user confirms they are correct

paths
while [ "$PATHCONF" != "Y" ]; do
paths
done

green_label=$"green"
amber_label=$"amber"
red_label=$"red"
crisis_label=$"crisis"

## generate label folders
mkdir $CORPUS_LABEL/$green_label
GREEN=$CORPUS_LABEL/$green_label
mkdir $CORPUS_LABEL/$amber_label
AMBER=$CORPUS_LABEL/$amber_label
mkdir $CORPUS_LABEL/$red_label
RED=$CORPUS_LABEL/$red_label
mkdir $CORPUS_LABEL/$crisis_label
CRISIS=$CORPUS_LABEL/$crisis_label


## iterates over each file in $CORPUS_ROOT
## finds IDs listed in $LABEL_FILE and inserts label in posts
## copies labeled posts to $CORPUS_LABEL

for file in $CORPUS_ROOT/*
  do
  thisFile=$(basename "$file") #get complete filename
  thisFileName="${thisFile%.*}" #get only filename, before extension
  thisFileID=${thisFileName:5} #get ID after "post-"

  value=$( grep -c -P "^"$thisFileID"\t[a-zA-Z0-9]*\t[a-zA-Z0-9]*" $LABEL_FILE ) #count grep from label file

  if [ $value -gt 0 ] #if any match was found, it is labeled
   then 
	echo "Hi, I was chosen! " $thisFileID 
	line=$(grep -P ""$thisFileID"\t[a-zA-Z0-9]*\t" $LABEL_FILE) #get line in label file with class
	echo "My class: " $line

		if [[ "$line" =~ "$crisis_label" ]] #if the label is found in the line,	
		then 
			sed -i "s/\/author>/\/author>\<clpsychlabel>$crisis_label\<\/clpsychlabel>/g" "$file" #insert label in post
			cp $file $CRISIS  #copy file to label folder
		echo "I was copied in: " $CRISIS
		
		elif [[ "$line" =~ "$red_label" ]]
		then 
			sed -i "s/\/author>/\/author>\<clpsychlabel>$red_label\<\/clpsychlabel>/g" "$file"
			cp $file $RED
		echo "I was copied in: " $RED
		
		elif [[ "$line" =~ "$amber_label" ]]
		then 
			sed -i "s/\/author>/\/author>\<clpsychlabel>$amber_label\<\/clpsychlabel>/g" "$file"
			cp $file $AMBER
		echo "I was copied in: " $AMBER
		
		else 
			sed -i "s/\/author>/\/author>\<clpsychlabel>$green_label\<\/clpsychlabel>/g" "$file"
			cp $file $GREEN
		echo "I was copied in: " $GREEN
		fi	

##for debug purposes:		
  #else
  # 	echo "I am this file: " $thisFileID " Sorry, I was not chosen :( "

  fi

done
echo "We are done here. Goodbye! :)"


