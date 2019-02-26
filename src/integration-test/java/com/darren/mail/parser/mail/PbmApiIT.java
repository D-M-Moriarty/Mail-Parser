package com.darren.mail.parser.mail;

import com.darren.mail.parser.entity.CustomerDetails;
import com.darren.mail.parser.pbmapi.PbmApi;
import com.darren.mail.parser.pbmapi.PbmApiService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PbmApiIT {

    @Mock
    private PbmApiService pbmApiService;
    @InjectMocks
    private PbmApi pbmApi;

    private List<CustomerDetails> customerDetails;
    private List<String> customerNumbers;

    @BeforeClass
    public static void beforeClass() {
        System.out.println("@BeforeClass - runOnceBeforeClass");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("@AfterClass - runOnceAfterClass");
    }

    @Before
    public void setup() {
        customerDetails =
                new ArrayList<CustomerDetails>(){{
                    add(new CustomerDetails("12345678", "tim@email.com"));
                }};
        customerNumbers =
                new ArrayList<String>(){{
                    add("12345678");
                    add("87654321");
                }};
    }

    @After
    public void tearDown() {
    }

    @Test
    public void checkGetCustomerDetailsByEmail_valid() {
        when(pbmApiService.retrieveCustomerDetailsByEmail(anyString())).thenReturn(customerDetails);
        List<CustomerDetails> customerDetails2 = pbmApi.getCustomerDetailsByEmail(customerDetails.get(0).getEmail());
        Assert.assertEquals(customerDetails.get(0).getCustNo(), customerDetails2.get(0).getCustNo());
    }

    @Test
    public void checkIsValidCustomerNumbers_valid() {
        when(pbmApiService.retrieveCustomerDetailsByCustomerNumber(anyString())).thenReturn(customerDetails);
        boolean isValid = pbmApi.isValidCustomerNumber(customerNumbers);
        Assert.assertTrue(isValid);
    }

    @Test
    public void checkIsValidCustomerNumber_invalid() {
        List<String> customerNumbers2 = new ArrayList<>();
        customerNumbers2.add("09876543");
        when(pbmApiService.retrieveCustomerDetailsByCustomerNumber(anyString())).thenReturn(new ArrayList<>());
        boolean isValid = pbmApi.isValidCustomerNumber(customerNumbers2);
        Assert.assertFalse(isValid);
    }

    @Test
    public void checkIsValidCustomerEmail_valid() {
        when(pbmApiService.retrieveCustomerDetailsByEmail(anyString())).thenReturn(customerDetails);
        List<CustomerDetails> customerDetails2 = pbmApi.getCustomerDetailsByEmail(customerDetails.get(0).getEmail());
        Assert.assertEquals(customerDetails.get(0).getCustNo(), customerDetails2.get(0).getCustNo());
    }

    @Test
    public void checkIsValidCustomerEmail_invalid() {
        String email = "tim@email.com";
        when(pbmApiService.retrieveCustomerDetailsByEmail(anyString())).thenReturn(customerDetails);
        List<CustomerDetails> customerDetails2 = pbmApi.getCustomerDetailsByEmail(email);
        Assert.assertEquals(customerDetails.get(0), customerDetails2.get(0));
    }

    @Test
    public void checkGetCustomerNumber() {
        List<String> customerNumbers2 = pbmApi.getCustomerNumbers(customerDetails);
        Assert.assertEquals(customerDetails.get(0).getCustNo(), customerNumbers2.get(0));
    }
}



