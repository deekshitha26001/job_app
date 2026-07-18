package backend.demo.discovery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyQualityFilterTest {

    private CompanyQualityFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CompanyQualityFilter();
    }

    // --- Should REJECT (non-company URLs) ---

    @Test
    void testRejectsForbes() {
        assertFalse(filter.isCompanyWebsite("https://www.forbes.com/lists/best-employers/"));
    }

    @Test
    void testRejectsLinkedIn() {
        assertFalse(filter.isCompanyWebsite("https://www.linkedin.com/company/phonepe"));
    }

    @Test
    void testRejectsWikipedia() {
        assertFalse(filter.isCompanyWebsite("https://en.wikipedia.org/wiki/Infosys"));
    }

    @Test
    void testRejectsMedium() {
        assertFalse(filter.isCompanyWebsite("https://medium.com/top-10-saas-companies-india"));
    }

    @Test
    void testRejectsCrunchbase() {
        assertFalse(filter.isCompanyWebsite("https://www.crunchbase.com/organization/razorpay"));
    }

    @Test
    void testRejectsBuiltIn() {
        assertFalse(filter.isCompanyWebsite("https://builtin.com/articles/top-tech-companies-india"));
    }

    @Test
    void testRejectsGlassdoor() {
        assertFalse(filter.isCompanyWebsite("https://www.glassdoor.com/Overview/Working-at-Freshworks"));
    }

    @Test
    void testRejectsInternshala() {
        assertFalse(filter.isCompanyWebsite("https://internshala.com/blog/top-it-companies-india/"));
    }

    @Test
    void testRejectsNaukri() {
        assertFalse(filter.isCompanyWebsite("https://www.naukri.com/companies"));
    }

    // --- Should ACCEPT (real company websites) ---

    @Test
    void testAcceptsPhonePe() {
        assertTrue(filter.isCompanyWebsite("https://www.phonepe.com"));
    }

    @Test
    void testAcceptsBrowserStack() {
        assertTrue(filter.isCompanyWebsite("https://www.browserstack.com"));
    }

    @Test
    void testAcceptsRazorpay() {
        assertTrue(filter.isCompanyWebsite("https://razorpay.com"));
    }

    @Test
    void testAcceptsFreshworks() {
        assertTrue(filter.isCompanyWebsite("https://www.freshworks.com"));
    }

    @Test
    void testAcceptsZoho() {
        assertTrue(filter.isCompanyWebsite("https://www.zoho.com"));
    }

    // --- Edge cases ---

    @Test
    void testRejectsNullUrl() {
        assertFalse(filter.isCompanyWebsite(null));
    }

    @Test
    void testRejectsBlankUrl() {
        assertFalse(filter.isCompanyWebsite("   "));
    }

    @Test
    void testIsNonCompanyUrlIsInverse() {
        assertTrue(filter.isNonCompanyUrl("https://www.forbes.com/list"));
        assertFalse(filter.isNonCompanyUrl("https://www.browserstack.com"));
    }
}
