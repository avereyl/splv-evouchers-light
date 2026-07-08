package org.splv.evouchers.core.service.tech.printing.datasource;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

public class EmptyRewindableDataSource implements JRRewindableDataSource {

	private int count = 1;
	private int index;

	@Override
	public boolean next() throws JRException {
		return (index++ < count);
	}

	@Override
	public void moveFirst() throws JRException {
		index = 0;
	}

	@Override
	public Object getFieldValue(JRField jrField) throws JRException {
		try {
			return PropertyUtils.getProperty(this, jrField.getName());
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException _) {
			return null;
		}
	}
}
