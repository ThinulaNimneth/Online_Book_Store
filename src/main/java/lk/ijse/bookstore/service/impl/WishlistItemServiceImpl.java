package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.entity.User;
import lk.ijse.bookstore.entity.WishlistItem;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.UserRepository;
import lk.ijse.bookstore.repository.WishlistItemRepository;
import lk.ijse.bookstore.service.WishlistItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistItemServiceImpl implements WishlistItemService {
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;


    @Override
    public void remove(String email, Long wishlistItemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.WISHLIST_ITEM_NOT_FOUND));

        if (!item.getWishlist().getUser().getUserId().equals(user.getUserId())) {
            throw new CustomerException(ResponseCode.FORBIDDEN, ResponseMessage.WISHLIST_ITEM_NOT_FOUND);
        }

        wishlistItemRepository.delete(item);
    }
}



