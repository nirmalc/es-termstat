Termstats plugin provides term frequency and document frequency for one or more indices specified in the request.

## Background
This plugin is inspired by jprante's termlist plugin (https://github.com/jprante/elasticsearch-index-termlist), which provides more features.
 I had to develop this plugin because of 
 - performance ( termlist plugin provides the ability to filter by term prefix, which this plugin does not - but that allows me to use more efficient Lucene code path)
 - elastic versions (7.2.x support)

## Status
At this time, this codebase supports only elasticsearch `7.2.x`. I'm adding more tests as well. I plan to migrate to newer versions soon ( PR's welcome)

## Building
```shell script
gradle test assemble  
```
## Installing
```shell script
bin/elasticsearch-plugin install file://<path_to_plugin>/es-termstat-7.2.0.zip
```

## Usage

Below API call gets the top 10 terms in an index by term frequency for a specific field.

```shell script
curl 'localhost:9200/spark/_termstat?pretty&field=content_en&size=5&sortByTF=true'
{
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "failed" : 0
  },
  "took" : 1782,
  "termstats" : [
    {
      "term" : "i",
      "termFrequency" : 10786032,
      "docFrequency" : 997123
    },
    {
      "term" : "you",
      "termFrequency" : 4653962,
      "docFrequency" : 841339
    },
    {
      "term" : "my",
      "termFrequency" : 4172296,
      "docFrequency" : 806449
    },
    {
      "term" : "have",
      "termFrequency" : 2818268,
      "docFrequency" : 821958
    },
    {
      "term" : "we",
      "termFrequency" : 2554484,
      "docFrequency" : 588529
    }
  ]
}

```

Sorting by document frequency is also supported

```shell script
curl 'localhost:9200/spark/_termstat?pretty&field=content_en&size=5&sortByDF=true'
{
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "failed" : 0
  },
  "took" : 588,
  "termstats" : [
    {
      "term" : "i",
      "termFrequency" : 10786032,
      "docFrequency" : 997123
    },
    {
      "term" : "you",
      "termFrequency" : 4653962,
      "docFrequency" : 841339
    },
    {
      "term" : "have",
      "termFrequency" : 2818268,
      "docFrequency" : 821958
    },
    {
      "term" : "from",
      "termFrequency" : 2051743,
      "docFrequency" : 812625
    },
    {
      "term" : "my",
      "termFrequency" : 4172296,
      "docFrequency" : 806449
    }
  ]
}

```

Params supported

- size ( how many terms we need back )
- sortByTF ( sorts by TF if this param is true, this is default)
- sortByDF ( sorts by Document Frequency if this param is true)
- field ( name of field to sort)

This supports multiple indices in the same request, but the response will look bit weirder as it combines high-frequency terms from both indices
