package ca.wimsc.client.common.util;

import org.junit.Assert;
import org.junit.Test;


public class StringUtilTest {

    @Test
    public void testAddAnchorTagsAroundLinks() {
        
        String input = "";
        String expect = "";
        String actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);

        input = "a";
        expect = "a";
        actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);

        input = "a http://example.com";
        expect = "a <a href=\"http://example.com\" target=\"_blank\">http://example.com</a>";
        actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);

        input = "a https://example.com";
        expect = "a <a href=\"https://example.com\" target=\"_blank\">https://example.com</a>";
        actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);

        input = "a http://example.com ";
        expect = "a <a href=\"http://example.com\" target=\"_blank\">http://example.com</a> ";
        actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);

        input = "a https://example.com ";
        expect = "a <a href=\"https://example.com\" target=\"_blank\">https://example.com</a> ";
        actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);

        input = "a http://example.com aaaa";
        expect = "a <a href=\"http://example.com\" target=\"_blank\">http://example.com</a> aaaa";
        actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);

        input = "a https://example.com aaaa";
        expect = "a <a href=\"https://example.com\" target=\"_blank\">https://example.com</a> aaaa";
        actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);
        
        input = "a https://example.com aaaa http://example.com bbbb";
        expect = "a <a href=\"https://example.com\" target=\"_blank\">https://example.com</a> aaaa <a href=\"http://example.com\" target=\"_blank\">http://example.com</a> bbbb";
        actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);

        input = "a http://example.com aaaa https://example.com bbbb";
        expect = "a <a href=\"http://example.com\" target=\"_blank\">http://example.com</a> aaaa <a href=\"https://example.com\" target=\"_blank\">https://example.com</a> bbbb";
        actual = StringUtil.addAnchorTagsAroundLinks(input);
        Assert.assertEquals(expect, actual);

    }
    
}
