package com.darren.mail.parser.pbmapi;

import com.darren.mail.parser.entity.CustomerDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PbmApi {

    //    @Autowired
    private PbmApiService pbmApiService;

    private static final Logger logger = LoggerFactory.getLogger(PbmApi.class);

    public PbmApi() { }

    public boolean isValidCustomerNumber(List<String> customerNumbers) {
        try {
            List<CustomerDetails> customerDetails;
            for (String customerNumber : customerNumbers) {
                customerDetails = pbmApiService.retrieveCustomerDetailsByCustomerNumber(customerNumber);
                if (customerDetails.isEmpty())
                    return false;
            }
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
        }
        return true;
    }

    public List<CustomerDetails> getCustomerDetailsByEmail(String customerEmail) {
        List<CustomerDetails> customerDetails = null;
        try {
            customerDetails = pbmApiService.retrieveCustomerDetailsByEmail(customerEmail);
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
        }
        return customerDetails;
    }

    public List<String> getCustomerNumbers(List<CustomerDetails> customerDetails) {
        List<String> customerNumbers = new ArrayList<>();
        for (CustomerDetails details: customerDetails) {
            customerNumbers.add(details.getCustNo());
        }
        return customerNumbers;
    }

}
