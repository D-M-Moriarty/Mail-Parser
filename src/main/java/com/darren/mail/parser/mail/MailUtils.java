package com.darren.mail.parser.mail;

import javax.mail.MessagingException;
import javax.mail.Part;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailUtils {

  protected MailUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static String formatIso8601(Date date) {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    // Quoted "Z" to indicate UTC, no timezone offset
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
    df.setTimeZone(tz);
    return df.format(date);
  }

  public static boolean isAttachmentOrInline(Part p) throws MessagingException {
    return Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition()) ||
        Part.INLINE.equalsIgnoreCase(p.getDisposition());
  }

  public static boolean isValidSubjectLine(String subject) {
    String expression = "\\[#\\d{8}]";
    Pattern pattern = Pattern.compile(expression);
    Matcher matcher = pattern.matcher(subject);
    return matcher.find();
  }

  public static String extractOriginalDateTime(String content) {
    String regex = "Sent: (?<date>.*?) To";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(content);
    if (matcher.find()) {
      return matcher.group("date");
    }
    return "No Date found";
  }

  public static List<String> extractCustomerNumbers(List<String> customerMatches) {
    List<String> custNos = new ArrayList<>();

    for (String custNo : customerMatches) {
      if (custNos.size() < 2) {
        custNo = custNo.replace("[#", "");
        custNo = custNo.replace("]", "");
        custNos.add(custNo);
      }
    }
    return custNos;
  }

  public static List<String> extractCustMatches(String subjectLine) {
    List<String> custNos = new ArrayList<>();
    String expression = "\\[#\\d{8}]";
    Pattern pattern = Pattern.compile(expression);
    Matcher matcher = pattern.matcher(subjectLine);
    while (matcher.find()) {
      custNos.add(matcher.group());
    }
    return custNos;
  }

  public static boolean isValidEmailAddress(String originalEmail) {
    return !originalEmail.equals("No Email Found");
  }

  public static String extractoriginalEmailAddress(String content) {
    String regex = "From: .+(?=<)\\<(?<originalEmail>.*?)\\>";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(content);
    if (matcher.find()) {
      return matcher.group("originalEmail");
    }
    return "No Email Found";
  }
}
