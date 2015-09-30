package com.savdev.jaxrs.service;

import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.savdev.jaxrs.boundary.UserDto;
import com.savdev.jaxrs.boundary.Validator;

/**
 */
public class UserService
{
    public static final String NEW_USER_CANNOT_HAVE_ID_FIELD = "New user cannot have an id field set";
    public static final String USER_NAME_CANNOT_BE_EMPTY = "User name cannot be null or empty";
    public static final String USER_AGE_IS_SMALL = "User age is too small";
    public static final String USER_CANNOT_BE_NULL = "User cannot be null";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String USER_LASTNAME_CANNOT_BE_EMPTY = "User lastname cannot be null or empty";
    public static final String WRONG_ID = "Entity ID cannot be null or empty";
    public static final String NOT_EXISTING_ID = "Cannot find entity with id = ";

    private static final Map<Integer, UserDto> cache = Maps.newConcurrentMap();

    public boolean exists(final int id)
    {
        return cache.containsKey(id);
    }

    public int create(final UserDto userDto)
    {
        final int key = new Random().nextInt(100);
        cache.put(key, userDto);
        return key;
    }

    public UserDto get(final int id)
    {
        if (!cache.containsKey(id))
        {
            throw new IllegalArgumentException(NOT_EXISTING_ID + id);
        }
        return cache.get(id);
    }

    public Validator validate(final UserDto user)
    {
        if (user == null)
        {
            throw new IllegalArgumentException(USER_CANNOT_BE_NULL);
        }

        final Validator validator = Validator.newInstance();
        boolean isValid = true;
        if (user.getId() != 0 )
        {
            isValid = false;
            validator.addError(NEW_USER_CANNOT_HAVE_ID_FIELD);
        }

        if (StringUtils.isEmpty(user.getName()))
        {
            isValid = false;
            validator.addError(USER_NAME_CANNOT_BE_EMPTY);
        }

        if (StringUtils.isEmpty(user.getLastName()))
        {
            isValid = false;
            validator.addError(USER_LASTNAME_CANNOT_BE_EMPTY);
        }

        if (user.getAge() <= 18)
        {
            isValid = false;
            validator.addError(USER_AGE_IS_SMALL);
        }

        if (isValid)
        {
            validator.markAsValid();
        }

        return validator;
    }
}
