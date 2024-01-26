package Interfaces;

import Model.DTO.UserLoginDTO;

import java.io.Serializable;
import java.rmi.RemoteException;

public interface RemoteLoginService extends Serializable {
    boolean loginUser(UserLoginDTO userLoginDTO) throws RemoteException;
}
