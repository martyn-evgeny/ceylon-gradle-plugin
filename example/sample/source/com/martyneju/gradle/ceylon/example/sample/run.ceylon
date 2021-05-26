import org.apache.logging.log4j {
	Logger,
    LogManager
}
import java.lang {
	Thread
}
import ceylon.interop.java { javaClass }

shared class MyTest() {}

Logger logger = LogManager.getLogger(javaClass<MyTest>());

shared void run() {
	logger.info("Hello world!");
	Thread.sleep(1000);
}