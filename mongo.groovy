import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import groovy.transform.Field
import io.codearte.jfairy.Fairy
import io.codearte.jfairy.producer.DateProducer
import io.codearte.jfairy.producer.text.TextProducer
import org.bson.Document

@Grab('io.codearte.jfairy:jfairy:0.4.0')
@Grab('org.mongodb:mongo-java-driver:3.0.2')

import java.util.logging.Logger

@Field Logger log = Logger.getLogger("")

@Field final int HOW_MANY = 1000000
@Field final int HOW_MANY_CACHE = 6
@Field MongoClient mongoClient = new MongoClient()
@Field MongoDatabase database = mongoClient.getDatabase('cache')
@Field MongoCollection<Document> apiCollection = database.getCollection('api')
@Field MongoCollection<Document> cacheCollection = database.getCollection('cache')

@Field Fairy fairy = Fairy.create(new Locale('PL'))
@Field DateProducer dateProducer = fairy.dateProducer()
@Field TextProducer textProducer = fairy.textProducer()

// MAIN
cleanCollections()
log.info 'creating api'
def apis = createApis()
log.info 'creating cache'
HOW_MANY_CACHE.times {
    createCacheKeys(apis)
}
log.info 'creating index'
createIndexes()

def cleanCollections() {
    apiCollection.deleteMany(new Document())
    cacheCollection.deleteMany(new Document())
}

private List createApis() {
    List<Document> apis = (1..HOW_MANY).collect {
        return new Document([
                _id    : UUID.randomUUID().toString(),
                _class: 'pl.mjasion.restcache.domain.Api',
        ])
    }
    apiCollection.insertMany(apis)
    return apis.collect {Document doc -> doc.getString('_id')}
}

private List createCacheKeys(List apis) {
    Random rand = new Random()
    List comments = apis.collect {
        def api = it
        new Document(
                api: api,
                key: textProducer.latinWord(1)+'_'+rand.nextLong(),
                value: textProducer.latinWord(4),
                _class: 'pl.mjasion.restcache.domain.Cache',
        )
    }
    log.info 'shuffling'
    Collections.shuffle(comments)
    cacheCollection.insertMany(comments)
}

private void createIndexes() {
    cacheCollection.createIndex(new Document('api': 1, 'key': 1))
}