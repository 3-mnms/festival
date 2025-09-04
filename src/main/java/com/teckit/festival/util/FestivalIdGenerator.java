package com.teckit.festival.util;

import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalIdGenerator {

    private final FestivalDetailRepository detailRepository;

    public String generateUniqueFid() {
        String fid;
        do {
            fid = "PF" + randomNumeric(6);
            if (detailRepository.existsById(fid)) {
                throw new BusinessException(ErrorCode.DUPLICATE_FID, "공연 식별자 생성 중 중복이 발생했습니다.");
            }
        } while (detailRepository.existsById(fid));
        return fid;
    }

    private String randomNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append((int) (Math.random() * 10));
        }
        return builder.toString();
    }
}