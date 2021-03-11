package socialnetwork.domain.validators;

import socialnetwork.domain.Utilizator;

import java.util.ArrayList;
import java.util.List;

public class UtilizatorValidator implements Validator<Utilizator> {
    List<String> errors = new ArrayList<>();
    @Override
    public void validate(Utilizator entity) throws ValidationException {
        errors.clear();
        if(entity.getLastName().length() == 0)
            errors.add("Numele nu poate fi vid");
        if(entity.getFirstName().length() == 0){
            errors.add("Prenumele nu poate fi vid");
        }
        if(errors.size() > 0)
            throw new ValidationException(errors);
    }
}
