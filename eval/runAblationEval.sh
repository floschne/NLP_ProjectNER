#!/bin/bash

EVALUATION_SCRIPT=./conlleval_ner.pl
EVALUATION_FILES=./*.txt
EVALUATION_OUTPUT=./evaluationOutput.txt
RESULT=./ablationResults.txt

runEval() {

    for f in $EVALUATION_FILES
    do
        echo "Processing $f ..."
        outputline=$(perl $EVALUATION_SCRIPT < $f | egrep "accuracy|./")
        outputline=$(echo "$outputline; extractors: $f")

        echo "$outputline" >> $EVALUATION_OUTPUT
    done
}

analyseResults() {
    
    awk -F';' 'BEGIN {print "accuracy;precision;extractors"}{print $1,";",$2,";",$5}' $EVALUATION_OUTPUT > $RESULT
    result=$(cat $RESULT | sed -e 's/.\/featureExtractorCombination_//g' | sed -e 's/.xml_evalOutput.txt//g' | sed -e 's/accuracy: //g' | sed 's/precision: //g' | sed 's/extractors: //g' | sed -e 's/ \+//g')
    echo "$result" | sort -r > $RESULT
    echo ""
    rm $EVALUATION_OUTPUT
    echo "Results saved to: $RESULT:"
    echo "#################"
    cat $RESULT

}

echo "Running evaluation script on all *.txt files in: $(pwd)"
echo "################################################"

    if [[ -e $EVALUATION_OUTPUT ]]; then
        rm $EVALUATION_OUTPUT
    fi
    
    if [[ -e $RESULT ]]; then
        rm $RESULT
    fi
runEval
analyseResults
