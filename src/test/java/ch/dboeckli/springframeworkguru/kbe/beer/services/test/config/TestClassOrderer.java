package ch.dboeckli.springframeworkguru.kbe.beer.services.test.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

@Slf4j
public class TestClassOrderer implements ClassOrderer {
    private static int getOrder(ClassDescriptor classDescriptor) {
        String className = classDescriptor.getTestClass().getSimpleName();
        return switch (className) {
            case String name when name.endsWith("IT") -> 2;
            case String name when name.endsWith("Test") || name.endsWith("Tests") -> 1;
            default -> Integer.MAX_VALUE;
        };
    }

    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(TestClassOrderer::getOrder));
    }
}
