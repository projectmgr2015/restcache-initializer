import groovy.transform.Field
import io.codearte.jfairy.Fairy
import io.codearte.jfairy.producer.text.TextProducer

@Grab('io.codearte.jfairy:jfairy:0.4.3')

import java.util.logging.Logger

@Field Logger log = Logger.getLogger("")

@Field final int HOW_MANY = 1_000_000

@Field Fairy fairy = Fairy.create(new Locale('PL'))
@Field TextProducer textProducer = fairy.textProducer()
@Field Random rand = new Random()

@Field File file = new File('data.csv')
file.text = ''

def data = (1..HOW_MANY).collect {
    createCsvLine()
}.join('\n')
file.append(data)

def createCsvLine() {
    String key = textProducer.latinWord(1) + '_' + rand.nextLong()
    String firstValue = getValue()
    String secondValue = getValue()
    return "$key,$firstValue,$secondValue"
}

def getValue() {
    return numericValue() ? rand.nextLong() : textProducer.latinWord(rand.nextInt(10) + 1)
}

def numericValue() {
    return rand.nextBoolean()
}
