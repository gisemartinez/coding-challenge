# Backend coding challenge


## The task

Develop a graphql api that serves the [schema](resources/backend.graphql) found in this repository.


This api is a single-tenant org chart api. You can load an orgchart's nodes and you can add new nodes. 

**Query**

While the query expects a flat structure, using the level attribute you should be able to filter out nodes above the specified level. 

If level = 0 , return all nodes

If level = 1 , return all but the root nodes

If level = n , return all nodes equal to or below level n

**Mutation**

The api only supports adding new nodes. 

Requirements: 
* only allow `n` direct children nodes per parent node, where `n` should be configurable on the app level.
* Support multiple root nodes (Co-founders and Co-CEOs do exist :) )

When adding new nodes make sure to handle concurrency and either reject concurrent requests or handle them as best as you can. 
**The org chart structure should never be corrupted**

## Technology requirements 

* Use sbt 
* Use [Sangria](https://sangria-graphql.github.io/) or [Caliban](https://github.com/ghostdogpr/caliban)
* Use postgres and a typed sql library like [Slick](https://github.com/slick/slick) or [Quill](https://getquill.io/#docs) to persist the data
* Make sure the api is handling concurrency well
* (Optional) Test either the api or the application logic with a real db instance 

A postgres instance can be started via the supplied compose file. 
You can put any table definition you need in the [init script](./resources/init_db.sql)

You can use Futures/Cats.IO/ZIO, any http server and testing library. 

Don't worry about the error handling in the api -> Just use whatever simple built in mechanism is provided by the graphql libs. 
