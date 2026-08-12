import { defineStore } from "pinia";
import { ref } from "vue";
import { getSiteContent } from "@/api/siteContent.js";

export const useSystemStore = defineStore("system", () => {
  const systemName = ref("家具商城");
  const systemTagline = ref("品质家居");
  const systemLogo = ref("");
  const contactInfo = ref({
    phone: "400-888-8888",
    email: "service@woodspace.com",
  });

  let loaded = false;
  let loadPromise = null;

  // 站点内容接口原始数据，供各组件共享，避免重复请求
  const siteData = ref(null);

  const load = () => {
    if (loaded) return Promise.resolve();
    // 并发调用共享同一个请求，防止重复发起
    if (!loadPromise) {
      loadPromise = (async () => {
        try {
          const res = await getSiteContent();
          if (!(res.success || res.code === 200) || !res.data) return;
          siteData.value = res.data;

          // Brand
          const brand = res.data.brand || [];
          const name = brand.find((b) => b.sectionKey === "system_name");
          if (name) systemName.value = name.contentTitle;
          const tag = brand.find((b) => b.sectionKey === "system_tagline");
          if (tag) systemTagline.value = tag.contentTitle;
          const logo = brand.find((b) => b.sectionKey === "system_logo");
          if (logo?.imageUrl) systemLogo.value = logo.imageUrl;

          // Contact
          const ct = (res.data.contact || [])[0];
          if (ct) {
            try {
              const extra = JSON.parse(ct.extraData || "{}");
              contactInfo.value = {
                phone: extra.phone || contactInfo.value.phone,
                email: extra.email || contactInfo.value.email,
              };
            } catch {
              /* ignore */
            }
          }

          loaded = true;
        } catch {
          /* ignore */
        } finally {
          loadPromise = null;
        }
      })();
    }
    return loadPromise;
  };

  return {
    systemName,
    systemTagline,
    systemLogo,
    contactInfo,
    siteData,
    load,
  };
});
