Bryan Irlbeck Project 6 DIY Distributed App

How the code was designed:
demo.java
	-runs the code, setting up the server using args[0] for the port 
	 number. if the number is invalid, it quits with an error message
	-if the server number is ok, it starts up the server and launches
	 a DIYAppController thread
	 
DIYAppController.java
	-constructor sets the public serversocket to the argument and a 
	 boolean finished flag to false until it is referenced again in 
	 the receptionist method finishMaybe()
	-public receptionist thread is started and set to daemon mode to 
	 run and accept workers that connect (passes over the serversocket
	 and reentrant lock to the waiter)
	-initialized blockingqueue with a limit of 1000, and sets the Shared sum to 0
	-starts to read in data from infile and adds dataslices(custom class) to 
	 the linkedblockingqueue in dataslices with 50 floats each
	-tries to add the slice to the queue, if .add throws an exception, it
	 catches it and tries again (it will always add without using the reentrant lock
	-after all the slices are added, it closes the infile and tries to add the poison
	-after this, it will sit and wait for all the workers to complete and then
	 just prints out the sum (workers finishing is determined by a shared attribute
	 called Shared.sum
	 
Receptionist.java
	-runs in a while loop accepting new workers until the server shuts down
	-if a worker is found, starts a waiter with a worker and the reentrantlock in the constructor
	
Waiter.java
	-initializes with the reentrant lock and adds 1 to the Shared.workerCount
	-sets up input/output streams and reads in a partial sum
	 that the worker sends first (then it starts sending data)
	-during the sending of data, it tries to acquire the reentrant lock, and if successful,
	 reads in the next data slice with peek();
	 -if it is null, it waits for a non null slice
	 -if it is the poison, it sends the poison to the worker and waits for the 
	  worker to confirm it is shutting down
	-if it is just a dataslice with no speical conditions, it adds the partial sum and sends the next slice
	 and then gives up the lock for other workers to access the queue and add to the Shared.sum

DIYAppWorker.java
	-establishes the connection to the server with args[0] and args[1]
	-once connected, it sends a parital sum of -1 to indicate it needs data
	-if its not the first iteration, it reads in the dataslice and works on it
	-if the dataslice is size of 0, it recognizes it as the poison and writes to the waiter
	 that it got the poison. then it shuts down and decreases the Shared.workerCount
	 
DataSlice.java
	-custom class to send data over the input/output streams effectively

PartialSum.java
	-same thing as DataSlice.java
	
Shared.java
	-holds the data of the slicequeue, sum, and workerCount
	-accessible by all of the classes, but needs to be accessed with a 
	 reentrant lock so no collisions happen at the critical section
	-holds the active amount of workers in workerCount (this is the ending condition)
	 -once workerCount reaches zero, the controller knows to quit
	
How to run the code:
	-compile with javac demo.java and run the demo with and run with a port number for the server (ex: java demo 6060)
	-compile with javac DIYAppWorker.java and run the worker with an address and port number for the server (ex: java DIYAppWorker localhost 6060)
	-it should start up with a message and communicate with the server 
	 to finish the sum. It worked with localhost and 6060 as the address and
	 port number, so I think it runs with no issues.