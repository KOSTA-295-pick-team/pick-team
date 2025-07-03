package com.pickteam.repository.common;

import com.pickteam.domain.common.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    /**
     * 해시된 파일명으로 FileInfo 조회 (삭제되지 않은 것만)
     * 
     * @param nameHashed 해시된 파일명
     * @return 조회된 FileInfo (Optional)
     */
    Optional<FileInfo> findByNameHashedAndIsDeletedFalse(String nameHashed);
}