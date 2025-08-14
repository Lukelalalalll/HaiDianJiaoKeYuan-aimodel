package com.zklcsoftware.basic.model;

import java.util.List;
import org.springframework.data.domain.Page;
import lombok.Getter;

@Getter
public class PageWrapper<T>
{

    private Page<T> page;
    private boolean first;
    private boolean last;
    private int number;
    private int pageFrom;
    private int pageTo;
    private int previousPage;
    private int nextPage;
    private int totalPages;
    private List<T> content;
    private String url;
    private Long total;


    public PageWrapper(Page<T> page, String url)
    {
        this.page = page;
        this.url = url;
        first = page.isFirst();
        last = page.isLast();
        this.total=page.getTotalElements();
        number = page.getNumber();
        totalPages = page.getTotalPages();
        content = page.getContent();
        previousPage = number - 1;
        if(previousPage < 0)
        {
            previousPage = 0;
        }
        nextPage = number + 1;
        if(nextPage >= totalPages)
        {
            nextPage = totalPages - 1;
        }
        pageFrom = number - 2;
        if(pageFrom < 0)
        {
            pageFrom = 0;
        }
        pageTo = pageFrom + 4;
        if(pageTo > totalPages - 1)
        {
            pageTo = totalPages - 1;
            pageFrom = pageTo - 4;
            if(pageFrom < 0)
            {
                pageFrom = 0;
            }
        }
    }


    public PageWrapper(Page<T> page)
    {
        this.page = page;
        first = page.isFirst();
        last = page.isLast();
        this.total=page.getTotalElements();
        number = page.getNumber();
        totalPages = page.getTotalPages();
        content = page.getContent();
        previousPage = number - 1;
        if(previousPage < 0)
        {
            previousPage = 0;
        }
        nextPage = number + 1;
        if(nextPage >= totalPages)
        {
            nextPage = totalPages - 1;
        }
        pageFrom = number - 2;
        if(pageFrom < 0)
        {
            pageFrom = 0;
        }
        pageTo = pageFrom + 4;
        if(pageTo > totalPages - 1)
        {
            pageTo = totalPages - 1;
            pageFrom = pageTo - 4;
            if(pageFrom < 0)
            {
                pageFrom = 0;
            }
        }
    }
}
