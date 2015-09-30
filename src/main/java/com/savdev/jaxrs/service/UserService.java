package com.savdev.jaxrs.service;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
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
    public static final String CANNOT_FIND_ENTITY = "Cannot find entity with id = ";

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
            throw new IllegalArgumentException(CANNOT_FIND_ENTITY + id);
        }
        return cache.get(id);
    }

    public void update(final int id, final UserDto user)
    {
        cache.put(id, user);
    }

    public void delete(final int id)
    {
        cache.remove(id);
    }

    public List<UserDto> getAll(final int offset, final int maxResult)
    {
        return Lists.newLinkedList(cache.values());
    }

    public List<UserDto> getAll()
    {
        //return all in one page:
        return getAll(0, 0);
    }

    public int numberOfPages(final int recordsPerPage)
    {
        int fullPagesNumb = cache.size() / recordsPerPage;
        if (cache.size() % recordsPerPage != 0)
        {
            fullPagesNumb++;
        }

        return fullPagesNumb;
    }

    public Validator validate4Create(final UserDto user)
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
        return validate(user, validator, isValid);
    }

    public Validator validate4Update(final UserDto user)
    {
        if (user == null)
        {
            throw new IllegalArgumentException(USER_CANNOT_BE_NULL);
        }
        final Validator validator = Validator.newInstance();
        return validate(user, validator, true);
    }

    private Validator validate(final UserDto user, final Validator validator, final boolean isValid)
    {
        boolean currentIsValid = isValid;
        if (StringUtils.isEmpty(user.getName()))
        {
            currentIsValid = false;
            validator.addError(USER_NAME_CANNOT_BE_EMPTY);
        }

        if (StringUtils.isEmpty(user.getLastName()))
        {
            currentIsValid = false;
            validator.addError(USER_LASTNAME_CANNOT_BE_EMPTY);
        }

        if (user.getAge() <= 18)
        {
            currentIsValid = false;
            validator.addError(USER_AGE_IS_SMALL);
        }

        if (currentIsValid)
        {
            validator.markAsValid();
        }

        return validator;
    }
}
