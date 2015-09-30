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
    public static UserDto userDto1, userDto2, userDto3;
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
    }



    @Override
    public boolean exists(final int notused)
    {
        return true;
    }

    @Override
    public UserDto get(final int id)
    {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        return userDto;
    }

    @Override
    public List<UserDto> getAll()
    {
        return Lists.newArrayList(userDto1, userDto2, userDto3);
    }
}
