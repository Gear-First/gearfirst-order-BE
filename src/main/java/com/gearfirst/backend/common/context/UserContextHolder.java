package com.gearfirst.backend.common.context;

/**
 * 인증된 사용자 정보를 어디서나 꺼내쓸 수 있게 보관하는 헬퍼 클래스(전역접근지점)
 */
public class UserContextHolder {
    //요청 스레드별로 분리된 저장소 만듬(ThreadLocal)
    private static final  ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    //현재 스레드(현재 HTTP 요청 처리 흐름)에 UserContext를 저장
    public static void set(UserContext context) {
        CONTEXT.set(context);
    }
    //현재 스레드에 저장된 UserContext를 반환
    public static  UserContext get() {
        return CONTEXT.get();
    }
    //요청 처리 완료 후 반드시 호출해서 메모리 누수/컨텍스트 누수를 방지
    public static void clear() {
        CONTEXT.remove();
    }
}
