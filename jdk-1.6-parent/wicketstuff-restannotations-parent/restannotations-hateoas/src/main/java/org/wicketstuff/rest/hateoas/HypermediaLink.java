package org.wicketstuff.rest.hateoas;

public class HypermediaLink
{
    private final String rel;
    private final String type;
    private final String href;
    
    public HypermediaLink(String rel, String type, String href)
    {
	this.rel = rel;
	this.type = type;
	this.href = href;
    }

    public String getRel()
    {
        return rel;
    }

    public String getType()
    {
        return type;
    }

    public String getHref()
    {
        return href;
    }

}
