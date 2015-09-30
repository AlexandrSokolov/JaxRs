package com.savdev.jaxrs.service;

import javax.enterprise.inject.Specializes;

import com.savdev.jaxrs.boundary.UserDto;

/**
 */
@Specializes
public class UserServiceMockUserAlreadyExists extends UserService
{
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
}
