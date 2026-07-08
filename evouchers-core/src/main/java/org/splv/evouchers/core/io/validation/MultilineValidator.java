package org.splv.evouchers.core.io.validation;

import java.util.Arrays;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MultilineValidator implements ConstraintValidator<MultilineConstraint, String> {

	private int nbLinesMax;
	private int lineMinLength;
	private int lineMaxLength;
	 
    @Override
    public void initialize(MultilineConstraint constraint) {
    	nbLinesMax = constraint.maxLines();
    	lineMinLength = constraint.minLengthPerLine();
    	lineMaxLength = constraint.maxLengthPerLine();
    }
	
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return lineMinLength == 0 ;
		}
		String[] lines = value.split("\n");
		return (lines.length <= nbLinesMax) && Arrays.asList(value.split("\n")).stream().allMatch(this::isLineValid);
	}
	
	private boolean isLineValid(String line) {
		return line.length() >= lineMinLength && line.length() <= lineMaxLength;
	}

}
