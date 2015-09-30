package com.savdev.jaxrs.service;

import javax.enterprise.inject.Specializes;

/**
 */
@Specializes
public class UserServiceMockUserAlreadyExists extends UserService
{
    @Override
    public boolean exists(int id)
    {
        return true;
    }
}
