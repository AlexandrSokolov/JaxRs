package com.savdev.jaxrs.service;

import java.util.List;

import javax.enterprise.inject.Specializes;

import com.google.common.collect.Lists;
import com.savdev.jaxrs.boundary.UserDto;

/**
 */
@Specializes
public class UserServiceMockUserAlreadyExists extends UserService
{
    public static final int numberOfPages = 3;
    public static final int offset = 2;
    public static final int maxResults = 3;
    public static final int EXISTING_KEY = 100;

    public static UserDto userDto1, userDto2, userDto3, userDto4, existingUser;
    static {
        userDto1 = new UserDto();
        userDto1.setName("Alex");
        userDto1.setId(1);

        userDto2 = new UserDto();
        userDto2.setName("Alex2");
        userDto2.setId(2);

        userDto3 = new UserDto();
        userDto3.setName("Alex3");
        userDto3.setId(3);

        userDto4 = new UserDto();
        userDto4.setName("Alex4");
        userDto4.setId(4);

        existingUser = new UserDto();
        existingUser.setId(EXISTING_KEY);
    }



    @Override
    public boolean exists(final int notused)
    {
        return true;
    }

    @Override
    public UserDto get(final int id)
    {
        return existingUser;
    }

    @Override
    public List<UserDto> getAll(final int offset, final int maxResult)
    {
        return Lists.newArrayList(userDto1, userDto2, userDto4);
    }

    @Override
    public int numberOfPages(int recordsPerPage)
    {
        return numberOfPages;
    }

    @Override
    public List<UserDto> getAll()
    {
        return Lists.newArrayList(userDto1, userDto2, userDto3);
    }
}
