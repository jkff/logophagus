#!/bin/bash
for ((i=0; i <= 10000 ; i++))
do
	echo "$(expr $i % 10) , text$i, text$i$i, \"text \"\"$i\"\" 
test\" , 1234fff";
done
