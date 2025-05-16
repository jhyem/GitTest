package com.example.project;
public class WastePriceCalculator {

    public static int calculatePrice(String category, float distance) {
        // 거리(m)를 cm로 변환
        float sizeCm = distance * 100;

        switch (category) {
            case "가전제품류 - TV":
                if (sizeCm < 63.5) return 3000; // 25인치 미만
                else if (sizeCm < 106.7) return 3600; // 42인치 미만
                else return 6000;

            case "가전제품류 - 공기청정기":
            case "가전제품류 - 식기건조기":
            case "가전제품류 - 식기세척기":
                return 3500;
            case "가전제품류 - 에어컨 실외기":
                return 2000;
            case "가전제품류 - 정수기":
                return 5000;

            case "가구류 - TV 받침대":
                return sizeCm < 120 ? 3000 : 5000;
            case "가구류 - 거울":
                return sizeCm * sizeCm < 10000 ? 1000 : 2000; // 1m^2 기준
            case "가구류 - 찬장":
                if (sizeCm < 90) return 3500;
                else if (sizeCm < 120) return 4000;
                else if (sizeCm < 130) return 7000;
                else return 10000;
            case "가구류 - 책상":
                return sizeCm < 120 ? 3000 : 5000;
            case "가구류 - 책장":
                if (sizeCm < 100) return 2000;
                else if (sizeCm < 180) return 5000;
                else return 9000;
            case "가구류 - 싱크대":
                return sizeCm < 120 ? 4000 : 6000;
            case "가구류 - 유리":
                return 2000; // m^2당 2,000원 (추가 계산 필요 시 확장)
            case "가구류 - 장롱":
                if (sizeCm < 90) return 7000;
                else if (sizeCm < 120) return 11900;
                else return 17900;
            case "가구류 - 장식장":
                if (sizeCm < 120) return 4000;
                else if (sizeCm < 180) return 5500;
                else return 10000;

            case "생활용품류 - 게시판 · 화이트보드":
                return sizeCm < 100 ? 1000 : 2000;
            case "생활용품류 - 고무통":
                if (sizeCm < 50) return 2000;
                else if (sizeCm < 100) return 3000;
                else return 5000;
            case "생활용품류 - 벽시계":
            case "생활용품류 - 빨래건조대":
                return sizeCm < 100 ? 1000 : 2000;
            case "생활용품류 - 액자":
                if (sizeCm < 50) return 1000;
                else if (sizeCm < 100) return 2000;
                else return 3000;
            case "생활용품류 - 어항":
                return sizeCm < 100 ? 4000 : 6000;
            case "생활용품류 - 조명기구":
                return sizeCm < 100 ? 1000 : 2000;
            case "생활용품류 - 캣타워":
                return sizeCm < 100 ? 3000 : 5000;
            case "생활용품류 - 항아리":
                return sizeCm < 50 ? 1000 : 2000;
            case "생활용품류 - 화분":
                return sizeCm < 50 ? 1000 : 1500;

            default:
                return 0; // 계산 불가 항목
        }
    }
}