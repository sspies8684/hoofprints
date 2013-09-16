package net.decix.bgpstack.types.pathattributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PathAttributeAnnotation
{
	int typeCode();
	String name();
	boolean optional() default false;
	boolean transitive() default true;
	boolean incomplete() default false; 
	boolean extendedLength() default false;

}
