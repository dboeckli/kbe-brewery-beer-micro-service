package ch.dboeckli.springframeworkguru.kbe.beer.services.test.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

@Slf4j
public class TestClassOrderer implements ClassOrderer {
    private static final String THIS_PACKAGE = TestClassOrderer.class.getPackageName();

    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(TestClassOrderer::getOrder));
    }

    private static int getOrder(ClassDescriptor classDescriptor) {
        Class<?> testClass = classDescriptor.getTestClass();
        String className = classDescriptor.getDisplayName();
        if (testClass.getPackageName().equals(THIS_PACKAGE)) {
            return 0;
        }

        if (className.endsWith("Test")) {
            return 1;
        } else if (className.endsWith("IT")) {
            return 2;
       } else {
            log.info("Test class {} does not end with 'Test', 'IT'", className);
            return 0;
        }
    }
}
