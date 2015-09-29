package com.savdev.jaxrs.boundary;

import java.util.List;

import com.google.common.collect.Lists;

/**
 */
public class Validator
{
    private boolean isValid = false;
    private List<String> errors = Lists.newArrayList();

    private Validator()
    {
    }

    public void markAsValid()
    {
        isValid = true;
    }

    public void markAsInvalid()
    {
        isValid = false;
    }

    public boolean isValid()
    {
        return isValid;
    }

    public boolean isNotValid()
    {
        return !isValid;
    }

    public void addError(final String error)
    {
        errors.add(error);
    }

    public List<String> getErrors()
    {
        return Lists.newArrayList(errors);
    }

    public static Validator newInstance()
    {
        final Validator validator = new Validator();
        return validator;
    }
}
