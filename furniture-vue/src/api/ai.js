import service from './request'

/**
 * AI客服对话（SSE流式响应）
 */
export function aiChatStream(data) {
    return service.post('/ai/chat/stream', data, {
        responseType: 'text',
        headers: {
            'Accept': 'text/event-stream'
        }
    })
}