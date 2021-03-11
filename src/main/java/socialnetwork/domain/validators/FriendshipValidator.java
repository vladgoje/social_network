package socialnetwork.domain.validators;

import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Utilizator;

public class FriendshipValidator implements Validator<Prietenie> {
    @Override
    public void validate(Prietenie entity) throws ValidationException {
        if(entity.getId().getLeft() <= 0)
            throw new ValidationException("ID nu poate fi negativ.");
        if(entity.getId().getRight() <= 0)
            throw new ValidationException("ID nu poate fi negativ.");
    }
}
