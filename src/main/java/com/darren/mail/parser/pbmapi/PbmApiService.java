package com.darren.mail.parser.pbmapi;

import com.darren.mail.parser.entity.CustomerDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//import org.springframework.cloud.netflix.ribbon.RibbonClient;
//import org.springframework.cloud.openfeign.FeignClient;

@Service
public interface PbmApiService {

    @GetMapping("customerNumber/{customerNumber}")
    List<CustomerDetails> retrieveCustomerDetailsByCustomerNumber(@PathVariable("customerNumber") String customerNumber);

    @GetMapping("email/{email}")
    List<CustomerDetails> retrieveCustomerDetailsByEmail(@PathVariable("email") String email);
}
