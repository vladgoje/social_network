package socialnetwork.domain.validators;

import socialnetwork.domain.Message;
import socialnetwork.domain.Utilizator;

import java.util.ArrayList;
import java.util.List;

public class MessageValidator implements Validator<Message>{
    List<String> errors = new ArrayList<>();
    @Override
    public void validate(Message entity) throws ValidationException {
        if(entity.getMessage().length() == 0)
            errors.add("Mesajul nu poate fi vid");
        if(errors.size() > 0)
            throw new ValidationException(errors);
    }

}
