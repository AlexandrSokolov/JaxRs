package com.savdev.jaxrs.boundary;

import java.util.List;

/**
 */
public class ListResource
{
    public static final String OFFSET_AND_MAX_RESULT_MUST_EXISTS = "Both 'offset' and 'maxResult' parameters must exist";

    //current page number
    private int offset;
    //page size
    private int maxResult;
    //number of pages
    private int numberOfPages;

    private List<?> items;

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public int getMaxResult()
    {
        return maxResult;
    }

    public void setMaxResult(int maxResult)
    {
        this.maxResult = maxResult;
    }

    public int getNumberOfPages()
    {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages)
    {
        this.numberOfPages = numberOfPages;
    }

    public List<?> getItems()
    {
        return items;
    }

    public void setItems(List<?> items)
    {
        this.items = items;
    }
}
