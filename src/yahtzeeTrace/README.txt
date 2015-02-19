To use the tracer:

	You can either import this folder into your project, or else copy the folder into your src folder and then refresh your project (f5 in Eclipse, or right click your project and click "refresh"). The files should show up as a yahtzeeTrace package with the following classes:
		-ScoreType.java
		-YahtzeeTrace.java
		-YahtzeeTracer.java
		-README.txt
	
	Note that the YahtzeeTracer.java is an empty stub that currently does nothing. The full implementation will be made available once I have done more testing.
	
	To use the tracer, make sure you import the yahtzeeTrace package into your server application:
	
	import yahtzeeTrace.*;
	
	Then, either in your constructor or initializer, instantiate a new YahtzeeTrace object in the following way:
	
	YahtzeeTrace yahtzeeTrace = new YahtzeeTracer();
	
	From there you use the callback functions to report the state of your system at certain times. There are additional details in the YahtzeeTrace.java interface, and we will go over some details in class.
	
	Any questions, comments, concerns, please email me at darrylhill@cmail.carleton.ca