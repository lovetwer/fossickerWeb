package com.fossicker.controller;

import com.fossicker.entity.AppVersion;
import com.fossicker.repository.AppVersionRepository;
import com.fossicker.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/version")
public class AppVersionController {

    @Autowired
    private AppVersionRepository appVersionRepository;

    @PostMapping("/create")
    public Result createVersion(@RequestBody AppVersion version) {
        if (version.getPlatform() == null || version.getVersionCode() == null || version.getVersionName() == null) {
            return Result.error(400, "平台、版本号、版本名称不能为空");
        }

        Optional<AppVersion> existingVersion = appVersionRepository.findByPlatformAndVersionCode(
                version.getPlatform(), version.getVersionCode());
        if (existingVersion.isPresent()) {
            return Result.error(400, "该版本已存在");
        }

        version.setStatus(0);
        version.setCreateTime(new Date());
        AppVersion savedVersion = appVersionRepository.save(version);
        return Result.success(savedVersion);
    }

    @PostMapping("/publish/{id}")
    public Result publishVersion(@PathVariable String id) {
        Optional<AppVersion> versionOpt = appVersionRepository.findById(id);
        if (versionOpt.isEmpty()) {
            return Result.error(404, "版本不存在");
        }

        AppVersion version = versionOpt.get();
        version.setStatus(1);
        version.setPublishTime(new Date());
        appVersionRepository.save(version);
        return Result.success("版本发布成功");
    }

    @PostMapping("/unpublish/{id}")
    public Result unpublishVersion(@PathVariable String id) {
        Optional<AppVersion> versionOpt = appVersionRepository.findById(id);
        if (versionOpt.isEmpty()) {
            return Result.error(404, "版本不存在");
        }

        AppVersion version = versionOpt.get();
        version.setStatus(2);
        appVersionRepository.save(version);
        return Result.success("版本下架成功");
    }

    @GetMapping("/list")
    public Result listVersions(@RequestParam(required = false) String platform) {
        List<AppVersion> versions;
        if (platform != null && !platform.isEmpty()) {
            versions = appVersionRepository.findByPlatformOrderByCreateTimeDesc(platform);
        } else {
            versions = appVersionRepository.findAllByOrderByCreateTimeDesc();
        }
        return Result.success(versions);
    }

    @GetMapping("/latest")
    public Result getLatestVersion(@RequestParam String platform) {
        Optional<AppVersion> versionOpt = appVersionRepository
                .findFirstByPlatformAndStatusOrderByVersionCodeDesc(platform, 1);

        if (versionOpt.isEmpty()) {
            return Result.error(404, "暂无可用版本");
        }

        return Result.success(versionOpt.get());
    }

    @PostMapping("/check")
    public Result checkUpdate(@RequestParam String platform, @RequestParam String versionCode) {
        // 获取该平台所有已发布的版本
        List<AppVersion> versions = appVersionRepository.findByPlatformAndStatus(platform, 1);

        if (versions.isEmpty()) {
            return Result.success(new UpdateCheckResult(false, false, null, "当前已是最新版本"));
        }

        // 找出版本号最大的
        AppVersion latestVersion = versions.get(0);
        for (AppVersion version : versions) {
            if (compareVersion(version.getVersionCode(), latestVersion.getVersionCode()) > 0) {
                latestVersion = version;
            }
        }

        if (compareVersion(versionCode, latestVersion.getVersionCode()) >= 0) {
            return Result.success(new UpdateCheckResult(false, false, null, "当前已是最新版本"));
        }

        boolean needForceUpdate = latestVersion.getForceUpdate() == 1;
        return Result.success(new UpdateCheckResult(true, needForceUpdate, latestVersion,
                needForceUpdate ? "发现新版本，请立即更新" : "发现新版本，建议更新"));
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteVersion(@PathVariable String id) {
        Optional<AppVersion> versionOpt = appVersionRepository.findById(id);
        if (versionOpt.isEmpty()) {
            return Result.error(404, "版本不存在");
        }

        appVersionRepository.deleteById(id);
        return Result.success("版本删除成功");
    }

    private int compareVersion(String v1, String v2) {
        // 标准化版本号
        int[] nums1 = parseVersion(v1);
        int[] nums2 = parseVersion(v2);

        // 逐段比较
        int maxLen = Math.max(nums1.length, nums2.length);
        for (int i = 0; i < maxLen; i++) {
            int num1 = i < nums1.length ? nums1[i] : 0;
            int num2 = i < nums2.length ? nums2[i] : 0;
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }

    private int[] parseVersion(String version) {
        if (version == null || version.isEmpty()) {
            return new int[]{0};
        }
        // 移除前缀 "v" 或 "V"
        version = version.replaceAll("^[vV]", "");
        // 按 . 或 - 分割
        String[] parts = version.split("[.-]");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                nums[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                nums[i] = 0;
            }
        }
        return nums;
    }

    public static class UpdateCheckResult {
        private boolean hasUpdate;
        private boolean forceUpdate;
        private AppVersion latestVersion;
        private String message;

        public UpdateCheckResult(boolean hasUpdate, boolean forceUpdate, AppVersion latestVersion, String message) {
            this.hasUpdate = hasUpdate;
            this.forceUpdate = forceUpdate;
            this.latestVersion = latestVersion;
            this.message = message;
        }

        public boolean isHasUpdate() {
            return hasUpdate;
        }

        public void setHasUpdate(boolean hasUpdate) {
            this.hasUpdate = hasUpdate;
        }

        public boolean isForceUpdate() {
            return forceUpdate;
        }

        public void setForceUpdate(boolean forceUpdate) {
            this.forceUpdate = forceUpdate;
        }

        public AppVersion getLatestVersion() {
            return latestVersion;
        }

        public void setLatestVersion(AppVersion latestVersion) {
            this.latestVersion = latestVersion;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
