package socialnetwork.service.validator;

import socialnetwork.domain.validators.ValidationException;

public class ServiceValidator {
    public void validateLong(String number){
        try{
            Long.parseLong(number);
        } catch (NumberFormatException e) {
            throw new ValidationException("Numar invalid.");
        }
    }
}
