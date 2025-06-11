#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define USE_SEQUENCE_OF_RESPONSES 0

#define SIM_COUNT 1000
#define TURN_COUNT_START 1
#define TURN_COUNT_END 100
#define START 0
#define END 5
#define AGREE_RATE 0

static const int first_layer_options[] = {-1, 0, 1};
static const int options[] = {-1, 1};

typedef enum
{
    WON = 1,
    LOST = 0
} score_e;

typedef enum
{
    AGREED = 1,
    DISAGREED = 0
} decision_e;

#if USE_SEQUENCE_OF_RESPONSES
static int8_t sequence_of_responses[] = { 1, 1, 1, 1, 1,     // +5
                                          0, 0, 0, 0, 0, 0,  // -6
                                          1, 1,              // +2
                                          0, 0, 0, 0,        // -4
                                          1, 1, 1, 1,        // +4
                                          0,                 // -1
                                          1,                 // +1
                                          0, 0};             // -2
#endif // USE_SEQUENCE_OF_RESPONSES

static int get_hint(int state)
{
    if (state == START + 1) return first_layer_options[rand() % 3];
    return options[rand() % 2];
}

static int disagree(int hint)
{
    return hint * -1;
}

static int move(int state)
{
    int hint;

    if (state == START)
    {
        return 1;
    }

    hint = get_hint(state);

    if (rand() % 100 < AGREE_RATE)
    {
        return hint;
    }
    else
    {
        return disagree(hint);
    }
}

#if USE_SEQUENCE_OF_RESPONSES
static int unconditional_move(int state, int8_t agreed) {
    const int hint = get_hint(state);

    if (agreed)
    {
        return hint;
    }
    else
    {
        return disagree(hint);
    }
}
#endif // USE_SEQUENCE_OF_RESPONSES

static score_e simulate(int turn_count, size_t *turns)
{
    int state = START;
    size_t i;
    for (i = 0; i < turn_count; i++)
    {
#if USE_SEQUENCE_OF_RESPONSES
        state += unconditional_move(state, sequence_of_responses[i]);
#else //USE_SEQUENCE_OF_RESPONSES
        state += move(state);
#endif // USE_SEQUENCE_OF_RESPONSES
        if (state == END)
        {
            *turns = i;
            return WON;
        }
    }
    *turns = i;
    return LOST;
}

int main(void)
{
#if USE_SEQUENCE_OF_RESPONSES
    if (TURN_COUNT_START > 25 || TURN_COUNT_END > 25) {
        fprintf(stderr, "We know only 25 replies\n");
        return -1;
    }
#endif // USE_SEQUENCE_OF_RESPONSES

    FILE *file = fopen("result.csv", "w");
    size_t won;
    size_t lost;
    size_t turns;
    size_t turns_avg = 0;

    srand(time(NULL));

    fprintf(file, "Ходов (максимум);Ходов (фактически);Побед;Поражений;Процент побед\n");

    for (size_t i = TURN_COUNT_START; i <= TURN_COUNT_END; i++)
    {
        won = 0;
        lost = 0;
        for (size_t j = 0; j < SIM_COUNT; j++)
        {
            if (simulate(i, &turns)) won++;
            else lost++;
            turns_avg += turns;
        }
        turns_avg /= SIM_COUNT;
        fprintf(file, "%lu;%lu;%lu;%lu;%.2f\n", i, turns_avg, won, lost, (double) won / (double) lost);
    }
    return 0;
}
