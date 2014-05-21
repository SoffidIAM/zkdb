package es.caib.zkib.datamodel.xml.validation;

import org.apache.commons.beanutils.BeanUtils;
import org.w3c.dom.Element;
import org.zkoss.lang.SystemException;
import org.zkoss.util.resource.Labels;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.definition.DefinitionInterface;
import es.caib.zkib.exceptions.ValidationException;

public class AttributeValidationDefinition implements DefinitionInterface, ValidatorInterface {
	String attribute;
	String friendlyName;
	String expr;
	
	public String getExpr() {
		return expr;
	}


	public void setExpr(String expr) {
		this.expr = expr;
	}


	public String getFriendlyName() {
		return friendlyName;
	}


	public String computeFriendlyName()
	{
		if (friendlyName == null)
			return attribute;
		else
		{
			try {
				return Labels.getRequiredLabel(friendlyName);
			} catch (SystemException e) {
				return friendlyName;
			}
		}

	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}


	boolean notNull = false;
	Integer maxLength;
	Double minValue;
	Double maxValue;
	
	
	public String getAttribute() {
		return attribute;
	}


	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}


	public boolean isNotNull() {
		return notNull;
	}


	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}


	public Integer getMaxLength() {
		return maxLength;
	}


	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}


	public Double getMinValue() {
		return minValue;
	}


	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}


	public Double getMaxValue() {
		return maxValue;
	}


	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}


	public void test(Element element) throws ParseException {
		if (attribute == null && expr == null)
			throw new ParseException (Messages.getString("AttributeValidationDefinition.ExpresionTag"), element); //$NON-NLS-1$
	}


	public void validate(DataContext ctx) throws Exception {
		Object result;
		if (expr == null)
		{
			Object obj = ctx.getData();
			result = BeanUtils.getProperty(obj, attribute);
		}
		else
		{
			result = Interpreter.evaluate(ctx, expr);
		}
		if ((result == null || result.toString().length() ==0 ) && isNotNull())
			throw new ValidationException(ctx.getCurrent(), ctx.getXPath()+"/"+attribute, String.format(Messages.getString("AttributeValidationDefinition.Cannotbeempty"), computeFriendlyName() )); //$NON-NLS-1$ //$NON-NLS-2$
		if (result != null && maxLength != null && result.toString().length() > maxLength.intValue())
			throw new ValidationException(ctx.getCurrent(), ctx.getXPath()+"/"+attribute, String.format(Messages.getString("AttributeValidationDefinition.CharactersMaximum"), computeFriendlyName(), maxLength )); //$NON-NLS-1$ //$NON-NLS-2$
		if (result != null && maxValue != null && Double.parseDouble(result.toString()) > maxValue.doubleValue())
			throw new ValidationException(ctx.getCurrent(), ctx.getXPath()+"/"+attribute, String.format(Messages.getString("AttributeValidationDefinition.Greater"), computeFriendlyName(), maxValue )); //$NON-NLS-1$ //$NON-NLS-2$
		if (result != null && minValue != null && Double.parseDouble(result.toString()) < minValue.doubleValue())
			throw new ValidationException(ctx.getCurrent(), ctx.getXPath()+"/"+attribute, String.format(Messages.getString("AttributeValidationDefinition.Less"), computeFriendlyName(), minValue )); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
