import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSiteContent } from '@/api/siteContent.js'

export const useSystemStore = defineStore('system', () => {
  const systemName = ref('WOODSPACE')
  const systemTagline = ref('品质家居')
  const systemLogo = ref('')
  const contactInfo = ref({ phone: '400-888-8888', email: 'service@woodspace.com' })

  let loaded = false

  const load = async () => {
    if (loaded) return
    try {
      const res = await getSiteContent()
      if (!(res.success || res.code === 200) || !res.data) return

      // Brand
      const brand = res.data.brand || []
      const name = brand.find(b => b.sectionKey === 'system_name')
      if (name) systemName.value = name.contentTitle
      const tag = brand.find(b => b.sectionKey === 'system_tagline')
      if (tag) systemTagline.value = tag.contentTitle
      const logo = brand.find(b => b.sectionKey === 'system_logo')
      if (logo?.imageUrl) systemLogo.value = logo.imageUrl

      // Contact
      const ct = (res.data.contact || [])[0]
      if (ct) {
        try {
          const extra = JSON.parse(ct.extraData || '{}')
          contactInfo.value = {
            phone: extra.phone || contactInfo.value.phone,
            email: extra.email || contactInfo.value.email
          }
        } catch { /* ignore */ }
      }

      loaded = true
    } catch { /* ignore */ }
  }

  return { systemName, systemTagline, systemLogo, contactInfo, load }
})
